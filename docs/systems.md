# System Outline (Map of Codebase)

## Loading System

Settings are stored in `minecraft/config/fpvdrone`.

`SettingsLoader` is responsible for reading and writing to disk. Also, once when reading, it will pass the loaded variables to other sub-systems.

The sub-systems `SettingsLoader` passes variables to are:

* `CameraManager` - Stores camera settings like which HUD components should appear.
* `ControllerConfig` - Stores controller settings like channel mapping and rates.
* `ControllerReader` - Stores controller name. The name is more tightly coupled to GLFW than the other controller settings, so putting it in ControllerConfig wasn't appropriate.
* `DroneBuild` - Stores the physical parameters of the drone.

### CameraManager

This is where the camera is manipulated.

`setSpectateCamera` and `setCamera` are Forge event handlers which is where the camera is manipulated.

The camera's rotation and position is set.

### ControllerConfig

A very simple class whose purpose is just for storing controller settings like channel mapping and rates. 

Getters, setters, resetters, etc are also included.

### ControllerReader

Contains all the complexity having to do with interfacing with GLFW.

The real-time stick positions and button states are also stored here.

### ControllerEvent

A container for Forge events related to the `ControllerReader`.

This is where GLFW is polled and the controller is updated.

### DroneBuild

A store for parameters which directly control the physical dimensions and properties of the drone.

For example, drone size, prop size, prop color, motor size, etc. are all stored here.

Both the Physics system and the Rendering system will be reading from `DroneBuild`.



## Networking System

The networking system has a similar job to the Loading System. It "loads" data from the internet.



## Physics System

A central feature of the physics system is the ability to swap out the core easily.

The core contains the equations of motion.

### PhysicsCore

The core must implement the `PhysicsCore` interface which contains a single function:

```typescript
function step(state: StateLens, dt: number): StateLens
```

The `step` function simply advances the physics state by one time step.

`dt` is the amount of time that should pass during that time step.

### PhysicsState

The physics system needs to read and write data from multiple external systems, so the PhysicsState is responsible for transporting data between the core and external systems. This allows the implementation of the core to be independent of the rest of the system. The cores only dependency is on the PhysicsState's schema/API, so the core can be isolated, but the cost is that it now requires more boilerplate just to expose a new variable to the physics core, but it's not that bad.

The PhysicsState does not contain state. It uses getters and setters to transport data between systems.

## Rendering System

// todo: Feel free anyone to fill this section out.
