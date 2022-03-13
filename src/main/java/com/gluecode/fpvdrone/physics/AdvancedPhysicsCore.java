package com.gluecode.fpvdrone.physics;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.input.MouseManager;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.gluecode.fpvdrone.util.Transforms;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.MathHelper;

public class AdvancedPhysicsCore implements IPhysicsCore {
    private static final float rads = (float) (Math.PI / 180);
    private static final float degs = (float) (180 / Math.PI);

    private static final float refTemp = 293.15f;

    private static float refResistance = 0.030f;
    private static float heatCap = 1f;
    private static float inertia = 1f;
    private static float motorSurfaceArea = 1f;
    private static float[] internalEnergy = new float[4];
    private static float[] temp = new float[4];
    private static float temp1 = refTemp;
    private static float temp2 = refTemp;
    private static float temp3 = refTemp;
    private static float temp4 = refTemp;
    private static float[] motorVel = new float[4];
    private static float[] motorPos = new float[4];
    private static Vector3f velocity = new Vector3f(0, 0, 0);

    private static Vector3f droneLook = new Vector3f(0, 0, 1);
    private static Vector3f droneUp = new Vector3f(0, 1, 0);
    private static Vector3f droneLeft = new Vector3f(1, 0, 0);

    private static boolean initDone = false;

    private static void init() {
        for (int i = 0; i < 4; i++) {
            internalEnergy[i] = 0;
            temp[i] = refTemp;
            motorVel[i] = 0;
            motorPos[i] = 0;
        }
    }

    @Override
    public void step(float dt) {
        if (!initDone) {
            init();
            initDone = true;
        }
        staticStep(dt);
    }

    @Override
    public float[] getMotorVel() {
        return motorVel;
    }

    @Override
    public float[] getMotorPos() {
        return motorPos;
    }

    @Override
    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public void setVelocity(Vector3f value) {
        velocity = value;
    }

    @Override
    public Vector3f getDroneLook() {
        return droneLook;
    }

    @Override
    public void setDroneLook(Vector3f value) {
        droneLook = value;
    }

    @Override
    public Vector3f getDroneUp() {
        return droneUp;
    }

    @Override
    public void setDroneUp(Vector3f up) {
        droneUp = up;
    }

    @Override
    public Vector3f getDroneLeft() {
        return droneLeft;
    }

    @Override
    public void setDroneLeft(Vector3f value) {
        droneLeft = value;
    }

    @Override
    public boolean isOverheat() {
        float resistance1 = getHotResistance(temp1);
        float resistance2 = getHotResistance(temp2);
        float resistance3 = getHotResistance(temp3);
        float resistance4 = getHotResistance(temp4);
        float averageRes = (resistance1 +
                resistance2 +
                resistance3 +
                resistance4) / 4f;
        return (refResistance / averageRes) < 0.5f;
    }

    private static void staticStep(float dt) {
        long tic = System.currentTimeMillis();

        rotateDrone(dt);

        Vector3f gravity = PhysicsState.getGravity();
        float mass = PhysicsState.getDroneMass();
        boolean flightMode3d = PhysicsState.getFlightMode3d();
        float throttle = PhysicsState.getThrottle();
        int batteryCells = PhysicsState.getBatteryCells();
        float kv = PhysicsState.getKv();
        int blades = PhysicsState.getBlades();
        float motorMass = PhysicsState.getMotorMass();
        float propRadius = PhysicsState.getPropRadius();
        float motorHeight = PhysicsState.getMotorHeight();
        float motorWidth = PhysicsState.getMotorWidth();

        float R = (motorWidth ) * 0.5f;
        float h = motorHeight ;
        float poles = getPoles(R * 2f);
        float slots = getSlots(R * 2f);
        float speed = velocity.length();

        setHeatCap(motorMass);
        setMotorSurfaceArea();
        setRefResistance(R, h, kv, poles, slots);
        setMotorInertia(blades, propRadius * 2f);


        // Drag is a force, so it needs to be converted to an acceleration before adding to gravity.
        // After converting drag is in b / t^2 so it needs to be added to acceleration after the acceleration is converted to b / t^2
        // The dragFactor is baked down from the drag coefficient definition, with the assumption that the drone is
        // equivalent to a 10cm angled cube.
        // 1.225 = mass density of air
        // .0314 = area of 10 cm disc
        // .8 = drag coefficient of angled cube
        // assume 1500 kg/m^3 density of drone (more dense than water but less than concrete)
        //        float area = 0.0314F;
        float radius = FastMath.pow(
                3f * mass /
                        (PhysicsConstants.batteryDensity * 4f * FastMath.PI),
                1f / 3f
        );
        float area = FastMath.PI * radius * radius;
        float dragCoefficient = 1.05F;
        float dragFactor = (PhysicsConstants.airDensity * area * dragCoefficient) /
                2F; // kg / m

        float throt = 0;
        if (flightMode3d) {
            throt = FastMath.clamp(
                    throttle,
                    -1f,
                    1.0F
            );
        } else {
            throt = FastMath.clamp(
                    (throttle + 1F) * 0.5F,
                    0,
                    1.0F
            );
        }

        float sag = MathHelper.lerp(FastMath.abs(throt), 4.0f, 3.6f);
        float vbat = sag * batteryCells * throt;

        float lowestAllowedFrameRate = 15f;
        int maxAllowedMillis = (int) (1000f / lowestAllowedFrameRate);

        if (!SettingsLoader.currentUseRealtimePhysics) {
            // assume 3 frames per tick, which is the case for 60 FDroneBuild.
            maxAllowedMillis *= 3;
        }

        float maxTimeStep = 1f / 128f;
        int maxIterations = (int) (dt / maxTimeStep) + 10;
        float timeProcessed = 0;
        for (int i = 0; i < maxIterations &&
                (dt - timeProcessed) > 0.00001f; i++) {
            float remainingTime = dt - timeProcessed;
            float timeStep = Math.min(maxTimeStep, remainingTime);

            Vector3f ambientDragForce = velocity.normalize().mult(-1f *
                    speed *
                    speed *
                    dragFactor); // m/s * m/s * kg / m = kg*m/s^2 div mass to get acceleration

            // netForce must be calculated before motorVel is mutated.
            Vector3f gravityForce = gravity.mult(mass);
            Vector3f netForce = gravityForce.add(new Vector3f(
                    (float) ambientDragForce.x,
                    (float) ambientDragForce.y,
                    (float) ambientDragForce.z
            ));

            for (int motor = 0; motor < 4; motor++) {
                //            Main.LOGGER.info("motorVel[motor]: " + motorVel[motor]);
                Vector3f[] results = getTotalForcesOnProp(
                        motor,
                        motorVel[motor]
                );
                Vector3f netPropForce = results[0];
                netForce.addLocal(netPropForce);


                float useVbat = (motor % 2 == 0 ? -1 : 1) * vbat;

                //        Main.LOGGER.info("netPropForce: " + netPropForce);
                //        Main.LOGGER.info("motor: " + motor);
                //        Main.LOGGER.info("motorVel[motor]: " + motorVel[motor]);
                //        Main.LOGGER.info("timeStep: " + timeStep);
                //        Main.LOGGER.info("useVbat: " + useVbat);
                //        Main.LOGGER.info("temp[motor]: " + temp[motor]);

                float nextMotorVel = advanceMotorVel(
                        motor,
                        motorVel[motor],
                        timeStep,
                        useVbat,
                        temp[motor]
                );
                //        Main.LOGGER.info("nextMotorVel: " + nextMotorVel);
                internalEnergy[motor] = getNextInternalEnergy(
                        motor,
                        internalEnergy[motor],
                        temp[motor],
                        useVbat,
                        motorVel[motor],
                        timeStep
                );
                temp[motor] = getNextTemp(internalEnergy[motor]);
                motorVel[motor] = nextMotorVel;
            }

            //      Main.LOGGER.info("mass: " + mass);
            Vector3f acceleration = netForce.mult(1f / mass);
            Vector3f nextVelocity = velocity.add(acceleration.mult(
                    timeStep)); // in tick units, 1 tick = 1, so accel = velo = dist
            //      Main.LOGGER.info("nextVelocity: " + nextVelocity);
            //      Main.LOGGER.info("timeStep: " + timeStep);
            velocity = PhysicsState.collideAndMove(nextVelocity, timeStep);
            //      position = PhysicsState.getPlayerPosition();

            if (System.currentTimeMillis() - tic > maxAllowedMillis) {
                Main.LOGGER.info("Physics simulation running behind!");
                break;
            }

            timeProcessed += timeStep;
        }
    }

    private static Vector3f getMotorToProp(
            float rotationAngle,
            Vector3f worldDroneUp,
            Vector3f worldDroneRight
    ) {
        Quaternion rot = (new Quaternion()).fromAngleAxis(
                rotationAngle,
                worldDroneUp
        );
        Vector3f propPositionRelMotor = rot.mult(worldDroneRight);
        return propPositionRelMotor;
    }

    /*
     * rotationAngle is the right-hand angle away from worldDroneRight.
     * */
    private static Vector3f getWorldPropVelocity(
            Vector3f worldDroneUp,
            Vector3f worldDroneVelocity,
            Vector3f motorToProp,
            float angularSpeed,
            float propRadius
    ) {
        Vector3f propVelocity = worldDroneUp.cross(motorToProp)
                .normalizeLocal()
                .multLocal(angularSpeed * propRadius);
        return propVelocity.addLocal(worldDroneVelocity);
    }

    private static Vector3f getFreeVelRelPropPlanar(
            Vector3f worldPropVelocity,
            Vector3f worldVelocityFree,
            Vector3f motorToProp
    ) {
        // ignore the component of wind blowing down the length of the prop since it contribute nearly 0s drag and lift.

        Vector3f freeVelRelProp = worldVelocityFree.subtract(worldPropVelocity);
        Vector3f normalProj = freeVelRelProp.project(motorToProp); // motorToProp is world space\
        return freeVelRelProp.subtractLocal(normalProj); // world space
    }

    /*
     * Everything should be in SI units.
     * */
    private static float getAoA(
            int motorNumber,
            float propPitch,
            float propRadius,
            Vector3f freeVelRelPropPlanar,
            Vector3f worldDroneUp,
            Vector3f motorToProp
    ) {
        float pitchAngle = FastMath.atan2(
                propPitch,
                2f * FastMath.PI * propRadius
        );
        // pitchAngle is relative to forwardSpinDirection

        // defined: direction wind is coming from is the opposite of the wind velocity:
        Vector3f windDirection = freeVelRelPropPlanar.mult(-1)
                .normalizeLocal(); // world space

        // todo, we should be able to do windAngle calculation in world space only.
        // At rotationAngle = 0, prop position 1, props-in,
        // pitchAngle increases like unit circle.
        // In motorToProp-space,
        // looking down -z direction.
        // Prop is moving towards positive x position.
        // Positive pitch results in more lift.
        // Likewise, windAngle is defined on the same space as pitchAngle.
        // windDirection would also be in positive x-direction assuming drone isn't moving and no free stream velocity.
        Vector3f forwardSpinDirection = worldDroneUp.cross(motorToProp); // motorToProp is world space.
        if (motorNumber % 2 == 0) {
            forwardSpinDirection.multLocal(-1);
        }
        float sign = FastMath.sign(worldDroneUp.dot(windDirection));
        if (sign == 0) {
            sign = 1;
        }
        float windAngle = sign *
                forwardSpinDirection.angleBetween(windDirection); // world-space comparison

        float angleOfAttack = pitchAngle - windAngle;

        if (motorNumber % 2 == 0) {
            angleOfAttack *= -1;
        }

        return angleOfAttack;
    }

    private static float getPropDragCoefficient(float aoa) {
        // http://www.aerospaceweb.org/question/airfoils/q0150b.shtml

        // Drag coefficient function is derived by curve fitting to real data.
        // The theory is that drag coefficient changes like the cos of the AoA because
        // if the AoA is 0, it has x dragC,
        // and if the AoA is 90, it has y dragC.
        // And if the AoA is somewhere between 0 and 90, then the drag is somewhere between x and y.
        // It is not a linear combination, like a lerp between x and y because we are dealing with a rotation here.
        // The part of the wing facing the wind which contributes to the x drag, and the part facing the wind which contributes the y drag can be called faces: xFace, yFace.
        // As the wing rotates, the dot product of xFace or yFace with the incident wind direction changes like cos.
        // If the assumption that xDrag and yDrag are linearly proportional to the dot product of xFace or yFace is true,
        // then xDrag and yDrag also vary like cos wrt to AoA.

        // The assumption is not entirely correct, but is still a close enough approximation.
        // Looking at real data, the drag is not perfectly a cos curve wtf AoA.
        // To make the curve fit better, a root of 1.15 was added.
        // This makes the drag grow more quickly than a regular cos as the AoA deviates from 0.

        float min = 0.4f;
        //    float min = 1f;
        float max = 1.8f;
        float minWrapped = FastMath.pow(min, 1.15f);
        float maxWrapped = FastMath.pow(max, 1.15f);
        float generalShape = -FastMath.cos(aoa * 2f) + 1f;
        float scaledAndOffset = generalShape / 2f * (maxWrapped - minWrapped) +
                minWrapped;
        float unwrappedShape = FastMath.pow(scaledAndOffset, 1f / 1.15f);
        return unwrappedShape;
    }

    private static float getPropLiftCoefficient(float aoa) {
        // this is a "tilted" sin wave. Look it up. Should be in a stackexchange post.
        // This derivation is very similar to the drag coefficient. It's sinusoidal for the same reason,
        // and it's "tiled" to match real world data cause by the special shape of the wing.
        // There is a point in a real wing where lift dips, around 20 AoA. This dip is not modelled.
        // Lift over drag:
        //(792/924 * sin(1*x*2)/1+495/924 * sin(2*x*2)/2+220/924 * sin(3*x*2)/3+66/924 * sin(4*x*2)/4+12/924 * sin(5*x*2)/5+1/924 * sin(6*x*2)/6) / ((-cos(x * 2) + 1) / 2 * (2 - 0.02) + 0.02)^(1/1.15)

        // Bound aoa to mod PI.
        if (aoa < 0) {
            aoa = FastMath.PI + (aoa % FastMath.PI);
        } else {
            aoa = aoa % FastMath.PI;
        }

        float pi = FastMath.PI;
        float alp = 15f / 2f * rads;
        float naca = FastMath.sqrt(2f / pi) * aoa * aoa * FastMath.exp(-(aoa *
                aoa) /
                (2f *
                        alp *
                        alp)) /
                (alp * alp * alp);
        naca = naca / 7f;

        float antiaoa = pi - aoa;
        float antinaca = -FastMath.sqrt(2f / pi) *
                antiaoa *
                antiaoa *
                FastMath.exp(-(antiaoa * antiaoa) / (2f * alp * alp)) /
                (alp * alp * alp);
        antinaca = antinaca / 7f;

        float plate = FastMath.sin(aoa * 2f);

        return (plate + naca + antinaca);

        //    float shape = 792f/924f * FastMath.sin(aoa*2f) + 495f/924f * FastMath.sin(2f*aoa*2f)/2f+220f/924f * FastMath.sin(3f*aoa*2f)/3f+66f/924f * FastMath.sin(4f*aoa*2f)/4f+12f/924f * FastMath.sin(5f*aoa*2f)/5f+1f/924f * FastMath.sin(6f*aoa*2f)/6f;
        //    return shape * 0.7f;
    }

    /*
     * Right now this function only computes the drag due to the component of free steam air parallel to droneUp.
     * Air flowing side-ways over the props is ignored.
     * This is a reasonable approximation because the props are spinning much faster than the side-ways free stream velocity.
     * So for example, consider a bi-prop. Assuming side-ways free stream velocity is 0.
     * One prop will experience side-ways drag going left, and the other prop will experience drag going right.
     * These drags should cancel out, but produce a resistance torque on the motor.
     * If there is a side-ways free stream velocity, then one prop will have more side-ways drag than the other.
     * I'm conjecturing that the difference in side-ways drags between the two props can be modelled as a net drag over the entire prop
     * which is equal to the nominal area of a non-rotating prop moving sideways through the air.
     * */
    private static Vector3f getPropDragForce(
            Vector3f freeVelRelPropPlanar,
            float dragCoefficient,
            float aoa,
            float sectionLength,
            float sectionWidth
    ) {
        float airDensity = 1.225F;

        float flowSpeed = freeVelRelPropPlanar.length();

        // assuming prop is a flat plate 15mm wide, 1 mm thick. Only radius/length is a user variable.
        // referenceArea is the area of the orthographic shadow of the object, as if the wind were parallel light rays.
        float thickness = 0.002f;
        float referenceArea = FastMath.abs(sectionLength *
                sectionWidth *
                FastMath.sin(aoa)) + FastMath.abs(
                sectionLength * thickness * FastMath.cos(aoa));

        float forceMag = dragCoefficient *
                airDensity *
                flowSpeed *
                flowSpeed *
                referenceArea / 2f;

        // drag direction is always the same as air flow direction. Note that this might not be parallel to droneUp.
        Vector3f dragForceRelProp = freeVelRelPropPlanar.normalize().multLocal(
                forceMag);

        //    // The drag force relative to the prop can be broken into components parallel to droneUp (forward motion of prop) and droneRight(rotational motion of prop).
        //    Vector3f dragForceRelDroneUp = dragForceRelProp.project(worldDroneUp);
        //    Vector3f dragForceRelPropRotation = dragForceRelProp.project(worldDroneRight);

        //    Main.LOGGER.debug(freeVelRelPropPlanar);

        return dragForceRelProp;
    }

    private static Vector3f getPropLiftForce(
            Vector3f freeVelRelPropPlanar,
            Vector3f motorToProp,
            float liftCoefficient,
            float sectionLength,
            float sectionWidth
    ) {
        float airDensity = 1.225F;

        float flowSpeed = freeVelRelPropPlanar.length();

        // referenceArea is the wing area.
        float referenceArea = sectionLength * sectionWidth;

        float forceMag = liftCoefficient *
                airDensity *
                flowSpeed *
                flowSpeed *
                referenceArea / 2f;

        //    if (motorNumber % 2 == 1) {
        //      forceMag *= -1;
        //    }

        // lift direction is always perpendicular to air flow direction.
        // Note lift force is always in direction of windDirection.cross(motorToProp),
        // assuming lift coefficient is positive.
        Vector3f windDirection = freeVelRelPropPlanar.mult(-1).normalizeLocal();
        Vector3f liftForceRelProp = motorToProp.normalize().crossLocal(
                windDirection).multLocal(forceMag);

        return liftForceRelProp;
    }

    private static Vector3f[] getTotalForcesOnProp(
            int motorNumber,
            float rotvel
    ) {
        float propPitchMeter = PhysicsState.getPropPitch();
        float propRadius = PhysicsState.getPropRadius();
        int blades = PhysicsState.getBlades();
        float motorWidth = PhysicsState.getMotorWidth();

        Vector3f netForce = new Vector3f(0, 0, 0);
        Vector3f netDragTorque = new Vector3f(0, 0, 0);

        Vector3f worldVelocityFree = new Vector3f(
                0,
                0,
                0
        ); // todo: allow non-zero world free velocity



        // Since prop spins so fast, the rotationAngle is in a practically random position each tick.
        //    float initRotationAngle = FastMath.nextRandomFloat() * 2f * FastMath.PI;
        float initRotationAngle = motorPos[motorNumber];
        for (int i = 0; i < blades; i++) {
            // get blade's rotationAngle
            float rotationAngle = initRotationAngle +
                    (360 * rads) * ((float) i) / ((float) blades);
            Vector3f motorToProp = getMotorToProp(
                    rotationAngle,
                    droneUp,
                    droneLeft.mult(-1)
            ); // world space

            // WORLD_DRONE_RIGHT IS ROTATION_ANGLE = 0

            // Force Thrust due to propeller: F = 2 * airDensity * propArea * velocityProp * (velocityProp - velocityFreeStream)
            // https://wright.nasa.gov/airplane/propth.html
            // The free stream velocity needs to be the component perpendicular to the prop.

            int nSections = 5; // This number should be half of the one used in PropModelRenderer
            float sectionLength = propRadius / ((float) nSections);
            for (int j = 0; j < nSections; j++) {
                // Measure from the middle of the section
                float sectionRadius = (j + 0.5f) / ((float) nSections) *
                        propRadius;

                if (sectionRadius < (motorWidth  * 0.5f)) {
                    // The part of the prop over the motor is not considered for drag or lift.
                    continue;
                }

                float sectionWidth = PhysicsState.getPropWidth();
                // Between motorWidth and 0 radius, y value interpolates to 0.
                // Note that motorWidth is twice as big as motorRadius
                if (0 <= sectionRadius &&
                        sectionRadius < (motorWidth )) {
                    float yscale = sectionRadius / (motorWidth );
                    sectionWidth *= yscale;
                }

                Vector3f worldVelocityDrone = velocity;

                // we break to prop up into sections length-wise because each section has a different aoa and flow velocity.
                Vector3f worldPropVelocity = getWorldPropVelocity(
                        droneUp,
                        worldVelocityDrone,
                        motorToProp,
                        rotvel,
                        sectionRadius
                );
                Vector3f freeVelRelPropPlanar = getFreeVelRelPropPlanar(
                        worldPropVelocity,
                        worldVelocityFree,
                        motorToProp
                );
                float aoa = getAoA(
                        motorNumber,
                        propPitchMeter,
                        sectionRadius,
                        freeVelRelPropPlanar,
                        droneUp,
                        motorToProp
                );
                float propDragCoefficient = getPropDragCoefficient(aoa);
                float propLiftCoefficient = getPropLiftCoefficient(aoa);
                Vector3f propDragForce = getPropDragForce(
                        freeVelRelPropPlanar,
                        propDragCoefficient,
                        aoa,
                        sectionLength,
                        sectionWidth
                );
                Vector3f propLiftForce = getPropLiftForce(
                        freeVelRelPropPlanar,
                        motorToProp,
                        propLiftCoefficient,
                        sectionLength,
                        sectionWidth
                );

                //        if (i == 0 && j == nSections - 1) {
                //          Main.LOGGER.info("rotvel: " + rotvel);
                //          Main.LOGGER.info("propLiftCoefficient: " + propLiftCoefficient);
                ////          Main.LOGGER.info("worldPropVelocity: " + worldPropVelocity);
                ////          Main.LOGGER.info("freeVelRelPropPlanar.mult(-1).normalizeLocal(): " + freeVelRelPropPlanar.mult(-1).normalizeLocal());
                //          Main.LOGGER.info("aoa: " + aoa * degs);
                ////          Main.LOGGER.info("rotvel: " + rotvel);
                ////          Main.LOGGER.info("propLiftForce: " + propLiftForce);
                ////          Main.LOGGER.info("propDragForce: " + propDragForce);
                //        }

                netDragTorque.addLocal(motorToProp.cross(propDragForce)
                        .multLocal(sectionRadius));
                netForce.addLocal(propDragForce);
                netForce.addLocal(propLiftForce);
            }
        }

        Vector3f[] results = new Vector3f[2];
        results[0] = netForce;
        results[1] = netDragTorque;
        return results;
    }

    private static float dflux_dt(
            float x,
            float slotW,
            float R,
            float h,
            float m,
            float rotvel
    ) {
        float gap = 0.00159f;

        float xlow = -slotW / 2f;
        float xhigh = slotW / 2f;

        float gmin = gap * gap + (x + xlow) * (x + xlow);
        float gmax = gap * gap + (x + xhigh) * (x + xhigh);

        float c1 = PhysicsConstants.vac * h * R * rotvel * m / (4f * FastMath.PI);
        float c2 = 3f * gap * gap;

        return (float) (c1 * (c2 * (Math.pow(gmax, -5f / 2f) - Math.pow(
                gmin,
                -5f / 2f
        )) - (Math.pow(gmax, -3f / 2f) - Math.pow(gmin, -3f / 2f))));
    }

    private static int getPoles(float motorWidth) {
        if (motorWidth >= 0.018f) {
            return 14;
        } else {
            return 12;
        }
    }

    private static int getSlots(float motorWidth) {
        if (motorWidth >= 0.018f) {
            return 12;
        } else {
            return 9;
        }
    }

    private static float getSlotW(float R, float slots) {
        return 2f * FastMath.PI * R / slots - 0.0005f;
    }

    private static float getMagW(float R, float poles) {
        return 2f * FastMath.PI * R / poles - 0.0005f;
    }

    private static float getPoleMagneticMoment(float R, float h, float poles) {
        float magW = getMagW(R, poles);
        float magD = 0.001f;
        float m = 1.48f / PhysicsConstants.vac * magW * h * magD;
        return m;
    }

    /*
    The number of loops of a single phase.
    A single phase may span multiple slots, but this is taken into account by kv.
    * */
    private static float getN(
            float R,
            float h,
            float kv,
            float poles,
            float slots
    ) {

        // These -0.0005 values are a little incorrect.
        // magW is being used for both magnetic moment and theta offset.
        // Only the magnetic moment should include the 0.0005f offfet,
        // but for some reason, the predictions are actually better if we have the offset applied to both.
        // It is technically incorrect, but practically it gives better results.
        float slotW = getSlotW(R, slots);
        float magW = getMagW(R, poles);
        float m = getPoleMagneticMoment(R, h, poles);

        float x1 = -magW + magW / 2f;
        float x2 = magW / 2f;
        float x3 = magW + magW / 2f;

        float d1 = dflux_dt(x1, slotW, R, h, m, 1f);
        float d2 = dflux_dt(x2, slotW, R, h, -m, 1f);
        float d3 = dflux_dt(x3, slotW, R, h, m, 1f);
        float d = d1 + d2 + d3;

        float N = Math.round(1f / (d * kv));
        return N;
    }

    private static void setHeatCap(float motorMass) {
        heatCap = PhysicsConstants.motorSpecificHeatCapacity * motorMass;
    }

    private static void setMotorSurfaceArea() {
        float motorWidth = PhysicsState.getMotorWidth();
        float motorHeight = PhysicsState.getMotorHeight();
        float R = (motorWidth ) * 0.5f;
        motorSurfaceArea = 2f * FastMath.PI * R * (motorHeight ) +
                2f * FastMath.PI * R * R;
    }

    private static void setMotorInertia(int nBlades, float propDiameter) {
        float motorWidth = PhysicsState.getMotorWidth();
        float bladeWidth = PhysicsState.getPropWidth();
        float bladeMass = PhysicsState.getBladeMass(propDiameter);
        float bellMass = PhysicsState.getBellMass();

        float bladeLength = propDiameter * 0.5f;
        float propInertia = nBlades / 12f *
                bladeMass *
                (4f * bladeLength * bladeLength +
                        bladeWidth * bladeWidth);
        float bellIntertia = bellMass *
                ((motorWidth ) * 0.5f) *
                ((motorWidth ) * 0.5f);
        inertia = propInertia + bellIntertia;
    }

    private static void setRefResistance(
            float R,
            float h,
            float kv,
            float poles,
            float slots
    ) {
        // These -0.0005 values are a little incorrect.
        // magW is being used for both magnetic moment and theta offset.
        // Only the magnetic moment should include the 0.0005f offfet,
        // but for some reason, the predictions are actually better if we have the offset applied to both.
        // It is technically incorrect, but practically it gives better results.


        float slotW = getSlotW(R, slots);
        float N = getN(R, h, kv, poles, slots);


        float loopPeri = 2f * h + 2f * slotW;
        float wireLength = N * loopPeri * PhysicsConstants.inches;
        refResistance = wireLength / 12f / 1000f *
                PhysicsConstants.resistancePer1000Ft;
    }

    private static float getHotResistance(float temp) {
        float tempDiff = temp - refTemp;
        //    return refResistance * (1 + 0.0393f * tempDiff);
        return refResistance * (1 + PhysicsConstants.tempCoeff * tempDiff);
    }

    private static float getCurrent(float resistance, float vbat, float rotvel) {
        // todo: inductance, impedance, harmonics
        float kv = PhysicsState.getKv();
        float backEMF = -rotvel / kv;
        return (vbat + backEMF) / resistance;
    }

    //  /*
    //  This is the moment for a single phase.
    //  In a BLDC, only a single phase is active at a time.
    //  Remember, N is the number of loops for a single phase.
    //  * */
    //  private static float getSlotMagneticMoment(float current, float R, float h, float slots, float N) {
    //    float slotW = getSlotW(R, slots);
    //    float slotArea = slotW * h;
    //    return N * current * slotArea;
    //  }

    private static float getTorque(float current, float kv) {
        return current / kv;
    }

    private static float getRotDrag(
            int motorNumber,
            float rotvel,
            float torque
    ) {
        Vector3f dragTorqueVector = getTotalForcesOnProp(
                motorNumber,
                rotvel
        )[1];
        float dragTorque = dragTorqueVector.dot(droneUp);
        return dragTorque;

        //    float friction = 0.001f * FastMath.sign(torque);
        //    if (FastMath.abs(friction) > FastMath.abs(torque)) {
        //      friction = torque;
        //    }
        //
        //    return -dragTorque - friction; // todo: not really sure why dragTorque has to be negated.
        //    return - 0.000000025f * (rotVel * rotVel) * FastMath.sign(rotVel);
    }

    private static float getRotAccel(
            int motorNumber,
            float rotvel,
            float vbat,
            float temp
    ) {
        float resistance = getHotResistance(temp);
        float kv = PhysicsState.getKv();
        float current = getCurrent(resistance, vbat, rotvel);
        float torque = getTorque(current, kv);
        float dragTorque = getRotDrag(motorNumber, rotvel, torque);
        float netTorque = torque + dragTorque;
        //    Main.LOGGER.info("resistance: " + resistance);
        //    Main.LOGGER.info("kv: " + kv);
        //    Main.LOGGER.info("current: " + current);
        //    Main.LOGGER.info("torque: " + torque);
        //    Main.LOGGER.info("dragTorque: " + dragTorque);
        //    Main.LOGGER.info("netTorque: " + netTorque);
        //    Main.LOGGER.info("inertia: " + inertia);
        return netTorque / inertia;
    }

    private static float advanceMotorVel(
            int motorNumber,
            float rotvel,
            float timeStep,
            float vbat,
            float temp
    ) {
        float k1 = getRotAccel(motorNumber, rotvel, vbat, temp);
        float k2 = getRotAccel(
                motorNumber,
                rotvel + (timeStep / 2f) * k1,
                vbat,
                temp
        );
        float k3 = getRotAccel(
                motorNumber,
                rotvel + (timeStep / 2f) * k2,
                vbat,
                temp
        );
        float k4 = getRotAccel(motorNumber, rotvel + timeStep * k3, vbat, temp);
        float nextVel = rotvel + timeStep * (k1 + 2f * k2 + 2f * k3 + k4) / 6f;

        //    Main.LOGGER.info("nextVel: " + nextVel);

        // nextVel = rotvel + getRotaccel(rotvel, vbat, temp) * timeStep;
        //    Main.LOGGER.info("nextVel: " + nextVel);

        //    Main.LOGGER.info("nextVel: " + nextVel);

        if (Float.isInfinite(nextVel)) {
            nextVel = 0;
        }
        if (Float.isNaN(nextVel)) {
            nextVel = 0;
        }
        float kv = (PhysicsState.getKv());
        float maxSpeed = kv * PhysicsState.getBatteryCells() * 4.2f;
        if (FastMath.abs(nextVel) > maxSpeed) {
            nextVel = maxSpeed * FastMath.sign(nextVel);
        }

        return nextVel;
    }

    private static float getInternalEnergyLost(float temp, float timeStep) {
        //    global refTemp;
        //    global heatCap;
        //    global heatTransfer;
        //    global surfaceArea;

        float timeConstant = heatCap /
                (PhysicsConstants.heatTransfer * motorSurfaceArea);
        float cooledTemp = refTemp + (temp - refTemp) * FastMath.exp(-timeStep /
                timeConstant);
        float deltaEnergy = heatCap * (cooledTemp - temp);
        return deltaEnergy;
    }


    private static float getInternalEnergyGained(
            float vbat,
            float rotvel,
            float timeStep
    ) {
        //    global refResistance;
        float current = getCurrent(refResistance, vbat, rotvel);
        float heatingPower = current * current * refResistance;
        float deltaEnergy = heatingPower * timeStep;
        return deltaEnergy;
    }

    private static float getNextInternalEnergy(
            int motorNumber,
            float internalEnergy,
            float tempin,
            float vbat,
            float rotvel,
            float timeStep
    ) {
        float nextEnergy = internalEnergy + getInternalEnergyLost(
                tempin,
                timeStep
        ) + getInternalEnergyGained(vbat, rotvel, timeStep);

        if (Float.isInfinite(nextEnergy)) {
            nextEnergy = 0;
            temp[motorNumber] = 0;
        }
        if (Float.isNaN(nextEnergy)) {
            nextEnergy = 0;
            temp[motorNumber] = 0;
        }

        return nextEnergy;
    }

    private static float getNextTemp(float nextInternalEnergy) {
        float nextTemp = nextInternalEnergy / heatCap + refTemp;
        return nextTemp;
    }

    private static void rotateDrone(float elapsed) {
        Minecraft minecraft = Minecraft.getInstance();

        float deadzone = 0; // todo: implement this as ControllerConfig.getDeadzone();

        float mouseYawDiff = MouseManager.yposDiff();
        float mousePitchDiff = MouseManager.getMousePitchDiff();
        float mouseRollDiff = MouseManager.getMouseRollDiff();

        // apply deadzone
        float yawDiff = Transforms.bfRate(
                ControllerReader.getYaw(),
                ControllerConfig
                        .getYawRate(),
                ControllerConfig.getYawSuper(),
                ControllerConfig.getYawExpo()
        ) *
                elapsed *
                rads + mouseYawDiff;
        float pitchDiff = Transforms.bfRate(
                ControllerReader.getPitch(),
                ControllerConfig.getPitchRate(),
                ControllerConfig.getPitchSuper(),
                ControllerConfig.getPitchExpo()
        ) * elapsed * rads + mousePitchDiff;
        float rollDiff = Transforms.bfRate(
                ControllerReader.getRoll(),
                ControllerConfig.getRollRate(),
                ControllerConfig.getRollSuper(),
                ControllerConfig.getRollExpo()
        ) *
                elapsed *
                rads + mouseRollDiff;
        if (Math.abs(yawDiff) < deadzone * elapsed * rads) {
            yawDiff = 0;
        }
        if (Math.abs(pitchDiff) < deadzone * elapsed * rads) {
            pitchDiff = 0;
        }
        if (Math.abs(rollDiff) < deadzone * elapsed * rads) {
            rollDiff = 0;
        }

        if (minecraft.options.getCameraType() ==
                PointOfView.THIRD_PERSON_FRONT) {
            //          yawDiff *= -1;
            pitchDiff *= -1;
            rollDiff *= -1;
        }

        // Update drone rotation. Needs to be done on frame render in order to avoid stutter.
        //        Main.LOGGER.debug(yawDiff);
        Quaternion yawRot = (new Quaternion()).fromAngleAxis(
                -yawDiff,
                droneUp
        );
        droneLook = yawRot.mult(droneLook);
        droneLeft = yawRot.mult(droneLeft);

        //        Main.LOGGER.debug(pitchDiff);
        Quaternion pitchRot = (new Quaternion()).fromAngleAxis(
                pitchDiff,
                droneLeft
        );
        droneLook = pitchRot.mult(droneLook);
        droneUp = pitchRot.mult(droneUp);

        //        Main.LOGGER.debug(rollDiff);
        Quaternion rollRot = (new Quaternion()).fromAngleAxis(
                rollDiff,
                droneLook
        );
        droneUp = rollRot.mult(droneUp);
        droneLeft = rollRot.mult(droneLeft);

        droneLook.normalizeLocal();
        droneUp.normalizeLocal();
        droneLeft.normalizeLocal();

        // todo: alternately set right or up equal to cross product.
        droneLeft = droneUp.cross(droneLook);
    }
}
