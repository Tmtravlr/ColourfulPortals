package com.tmtravlr.colourfulportalsmod;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ServerTickEvents
{
  @SubscribeEvent
  public void onServerTick(ServerTickEvent event)
  {
    if (event.phase == Phase.START)
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