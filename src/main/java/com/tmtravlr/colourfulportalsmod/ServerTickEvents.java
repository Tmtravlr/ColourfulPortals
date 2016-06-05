package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvents
{
  @SubscribeEvent
  public void onServerTick(ServerTickEvent event)
  {
    if (event.phase == TickEvent.Phase.START)
    {
      ColourfulPortalsMod cpMod = ColourfulPortalsMod.colourfulPortalsMod;
      if (MinecraftServer.getServer().getFolderName() != cpMod.currentFolder)
      {
        cpMod.loadPortalsList();
        cpMod.currentFolder = MinecraftServer.getServer().getFolderName();
      }
      BlockColourfulPortal.instance.serverTick();
    }
  }
}