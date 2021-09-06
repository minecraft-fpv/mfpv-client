package com.gluecode.fpvdrone.network;

import com.gluecode.fpvdrone.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import org.json.simple.JSONObject;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class VersionNotifier {
  public static final String VERSION_URL = "https://minecraftfpv-assets.s3.us-east-2.amazonaws.com/version.json";
  public static String versionRaw = null;
  public static JSONObject versionObject = null;
  public static String version = "";
  public static VersionChecker.CheckResult result = null;
  public static IModInfo info;
  
  public static boolean versionNotificationSent = false;
  
  public static void init() {
    info = ModLoadingContext.get().getActiveContainer().getModInfo();
    result = VersionChecker.getResult(info);
    
    //        AsyncHttpClient client = Dsl.asyncHttpClient();
    //        client.prepareGet(VERSION_URL).execute(new AsyncCompletionHandler<String>() {
    //
    //            @Override
    //            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
    //                versionRaw = new String(bodyPart.getBodyPartBytes(), StandardCharsets.UTF_8);
    //                versionObject = (JSONObject) (new JSONParser().parse(versionRaw));
    //
    //                Pattern pattern = Pattern.compile("1.15.2-recommended\": \"([^\"]*)\"");
    //                Matcher matcher = pattern.matcher(versionRaw);
    //                matcher.find();
    //                version = "1.15.2-" + matcher.group(1);
    //
    //                if (version.equalsIgnoreCase(info.getVersion().toString())) {
    //                    versionNotificationSent = true;
    //                }
    //
    //                return State.CONTINUE;
    //            }
    //
    //            @Override
    //            public String onCompleted(Response response) throws Exception {
    //                return versionRaw;
    //            }
    //        });
  }
  
  //    @SubscribeEvent
  //    public static void handleVersionNotification(TickEvent.PlayerTickEvent event) {
  //        if (event.phase == TickEvent.Phase.END) {
  //            if (!versionNotificationSent) {
  //                versionNotificationSent = true;
  //                event.player.sendMessage(new StringTextComponent("A newer version of the fpv-drone mod is on " +
  //                        "curseforge."));
  //            }
  //        }
  //    }
}
