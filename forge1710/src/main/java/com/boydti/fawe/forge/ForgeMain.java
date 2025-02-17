package com.boydti.fawe.forge;

import com.boydti.fawe.Fawe;
import com.boydti.fawe.config.Settings;
import com.boydti.fawe.forge.v1710.ForgeChunkUpdater;
import com.boydti.fawe.object.FawePlayer;
import com.sk89q.worldedit.extension.platform.CommandManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import java.io.File;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = "kawe", name = "KamAsyncWorldEdit", version = "1.0", acceptableRemoteVersions = "*", dependencies = "before:worldedit")
public class ForgeMain {
    private static FaweForge IMP;
    private Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        this.logger = event.getModLog();
        File directory = new File(event.getModConfigurationDirectory() + File.separator + "KamAsyncWorldEdit");
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        this.IMP = new FaweForge(this, event.getModLog(), event.getModMetadata(), directory);

        CommandManager.setChunkUpdater(new ForgeChunkUpdater());

        try {
            Class.forName("org.spongepowered.api.Sponge");
            Settings.IMP.QUEUE.PARALLEL_THREADS = 1;
        } catch (Throwable ignore) {}
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        IMP.insertCommands();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.worldObj.isRemote) {
            return;
        }
        handleQuit((EntityPlayerMP) event.player);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        for (EntityPlayerMP player : (List<EntityPlayerMP>)MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            handleQuit(player);
        }
    }

    public void handleQuit(EntityPlayerMP player) {
        FawePlayer fp = FawePlayer.wrap(player);
        if (fp != null) {
            fp.unregister();
        }
        Fawe.get().unregister(player.getCommandSenderName());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(EntityJoinWorldEvent event) {
        Entity entity = event.entity;
        if (!(entity instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) entity;
        if (player.worldObj.isRemote) {
            return;
        }
    }
}
