package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.client.registry.RenderingRegistry;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class ClientProxy
  extends CommonProxy
{
  public static int renderPass;
  
  public void registerSounds() {}
  
  public void registerRenderers()
  {
    System.out.println("cp - Loading the rendering handler.");
    
    RenderingRegistry.registerBlockHandler(ColourfulPortalsMod.standaloneRenderer);
  }
  
  public File getSaveLocation()
    throws IOException
  {
    File saveDirectory = new File(Minecraft.getMinecraft().mcDataDir, "saves/" + MinecraftServer.getServer().getFolderName());
    saveDirectory.mkdir();
    
    return new File(saveDirectory, "colourful_portal_locations.dat");
  }
}