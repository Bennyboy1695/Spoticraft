package mine.block.spoticraft.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.GuiUtils;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.net.URL;

public class SpotifyToast implements Toast {
    private long startTime;
    private boolean justUpdated;
    public final CurrentlyPlaying currentlyPlaying;
    private final NativeImage image;

    public SpotifyToast(CurrentlyPlaying currentlyPlaying) {
        this.currentlyPlaying = currentlyPlaying;

        if(currentlyPlaying.getItem() instanceof Track track) {
            ResourceLocation texID = new ResourceLocation("spotify", track.getId().toLowerCase());

            try {
                image = NativeImage.read(new URL(track.getAlbum().getImages()[0].getUrl()).openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Minecraft.getInstance().getTextureManager().register(texID, new DynamicTexture(image));
        } else if(currentlyPlaying.getItem() instanceof Episode episode) {
            ResourceLocation texID = new ResourceLocation("spotify", episode.getId().toLowerCase());

            try {
                image = NativeImage.read(new URL(episode.getImages()[0].getUrl()).openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Minecraft.getInstance().getTextureManager().register(texID, new DynamicTexture(image));
        } else {
            throw new RuntimeException("Invalid");
        }
    }

    @Override
    public int width() {
        int widthName = (int) (Minecraft.getInstance().gui.getFont().width(currentlyPlaying.getItem().getName()) * (0.75));
        int widthArtist = 0;

        if(currentlyPlaying.getItem() instanceof Track track) {
            widthArtist = (int) (Minecraft.getInstance().gui.getFont().width(new TextComponent(track.getArtists()[0].getName())) * (0.75));
        } else {
            widthArtist = (int) (Minecraft.getInstance().gui.getFont().width(new TextComponent(((Episode) currentlyPlaying.getItem()).getShow().getName())) * (0.75));
        }

        return 160 + Math.max(widthArtist, widthName);
    }

    @Override
    public int height() {
        return 38;
    }

    @Override
    public Visibility render(PoseStack poseStack, ToastComponent toastComponent, long startTime) {
        // 2,2 -> 35,35 image;
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        ResourceLocation texID = new ResourceLocation("spotify", currentlyPlaying.getItem().getId().toLowerCase());

        GuiComponent.fill(poseStack, 0, 0, this.width(), this.height(), 0xFF191414);

        RenderSystem.setShaderTexture(0, texID);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();

        GuiUtils.drawContinuousTexturedBox(poseStack, 2, 2, this.height() - 4, this.height() - 4, 0, 0, image.getWidth(), image.getHeight(), image.getWidth(), image.getHeight());

        RenderSystem.setShaderTexture(0, new ResourceLocation("spoticraft", "textures/spotify.png"));

        GuiUtils.drawContinuousTexturedBox(poseStack, this.width() - (16+8), (this.height() / 2)-8, 16, 16, 0, 0, 32, 32, 32, 32);

        RenderSystem.disableBlend();

        toastComponent.getMinecraft().gui.getFont().draw(poseStack, new TextComponent(currentlyPlaying.getItem().getName()), 43F, 10F, -256);

        if(currentlyPlaying.getItem() instanceof Track track) {
            toastComponent.getMinecraft().gui.getFont().draw(poseStack, new TextComponent(track.getArtists()[0].getName()), 43F, 21F, -1);
        } else {
            toastComponent.getMinecraft().gui.getFont().draw(poseStack, new TextComponent(((Episode) currentlyPlaying.getItem()).getShow().getName()), 43F, 21F, -1);
        }

        return startTime - this.startTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
}
