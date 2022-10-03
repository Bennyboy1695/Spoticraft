package mine.block.spoticraft.client;

import com.mojang.blaze3d.platform.NativeImage;
import mine.block.spoticraft.client.ui.SpotifyToast;
import mine.block.spotify.SpotifyHandler;
import mine.block.utils.LiveWriteProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.Environment;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.*;

@Mod("spoticraft")
@Mod.EventBusSubscriber(Dist.CLIENT)
public class SpoticraftClient {
    
    public static final LiveWriteProperties CONFIG = new LiveWriteProperties();
    public static final Logger LOGGER = LoggerFactory.getLogger("Spoticraft");
    public static final String VERSION = "1.1.0";
    public static boolean MC_LOADED = false;
    public static CurrentlyPlaying NOW_PLAYING = null;
    public static NativeImage NOW_ART = null;
    public static ResourceLocation NOW_ID = null;
    public static HashMap<ResourceLocation, NativeImage> TEXTURE = new HashMap<>();

    public SpoticraftClient() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitializeClient);
        });
    }

    public static void run(CurrentlyPlaying currentlyPlaying) {
        if(!MC_LOADED) return;
        if (NOW_PLAYING == null || !NOW_PLAYING.getItem().getId().equals(currentlyPlaying.getItem().getId())) {
            NOW_PLAYING = currentlyPlaying;

            var item = currentlyPlaying.getItem();

            ResourceLocation texture = new ResourceLocation("spotify", currentlyPlaying.getItem().getId().toLowerCase());

            if (!TEXTURE.containsKey(texture)) {
                if (item instanceof Track track) {
                    try {
                        NOW_ART = NativeImage.read(new URL(track.getAlbum().getImages()[0].getUrl()).openStream());
                        TEXTURE.put(texture, NOW_ART);
                        NOW_ID = texture;

                        Minecraft.getInstance().getTextureManager().register(NOW_ID, new DynamicTexture(NOW_ART));
                    } catch (IOException e) {
                        return;
                    }
                } else {
                    try {
                        NOW_ART = NativeImage.read(new URL(((Episode) currentlyPlaying.getItem()).getImages()[0].getUrl()).openStream());
                        TEXTURE.put(texture, NOW_ART);
                        NOW_ID = texture;
                        Minecraft.getInstance().getTextureManager().register(NOW_ID, new DynamicTexture(NOW_ART));
                    } catch (IOException e) {
                        return;
                    }
                }
            } else {
                NOW_ART = TEXTURE.get(texture);
                NOW_ID = texture;
            }


            if (Minecraft.getInstance().screen!= null && NOW_ART != null) {
                Minecraft.getInstance().getToasts().addToast(new SpotifyToast(currentlyPlaying));
            }
        }


    }

    @SubscribeEvent
    public void onTitleScreen(ScreenOpenEvent event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (!MC_LOADED) {
                MC_LOADED = true;
            }
        }
    }

    private void onInitializeClient(FMLClientSetupEvent event) {
        SpotifyHandler.setup();

        SpotifyHandler.PollingThread thread = new SpotifyHandler.PollingThread();
        ExecutorService checkTasksExecutorService = new ThreadPoolExecutor(1, 10,
                100000, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());
        checkTasksExecutorService.execute(thread);
        SpotifyHandler.songChangeEvent.add(SpoticraftClient::run);
/*        var key = KeyBinding.registerKeyBinding(new KeyBinding(
                "key.spotify.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "category.spotify.main"
        ));*/
    }
}
