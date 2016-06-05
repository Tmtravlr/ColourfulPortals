package com.tmtravlr.colourfulportalsmod;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy
  extends CommonProxy
{
  public static int renderPass;
  
  public void registerSounds() {}
  
  public void registerRenderers()
  {
	  Minecraft mc = Minecraft.getMinecraft();
	  mc.getBlockRendererDispatcher().getBlockModelShapes().registerBuiltInBlocks(ColourfulPortalsMod.colourfulWater);
	  
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.bucketColourfulWaterEmpty, 0, new ModelResourceLocation("colourfulportalsmod:bucket_colourful_water_first", "inventory"));
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.bucketColourfulWaterFirst, 0, new ModelResourceLocation("colourfulportalsmod:bucket_colourful_water_first", "inventory"));
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.bucketColourfulWater, 0, new ModelResourceLocation("colourfulportalsmod:bucket_colourful_water", "inventory"));
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.bucketColourfulWaterUnmixed, 0, new ModelResourceLocation("colourfulportalsmod:bucket_colourful_water_unmixed", "inventory"));
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.bucketColourfulWaterPartMixed, 0, new ModelResourceLocation("colourfulportalsmod:bucket_colourful_water_unmixed", "inventory"));
	  
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.enderPearlColoured, 0, new ModelResourceLocation("colourfulportalsmod:colourful_ender_pearl", "inventory"));
	  mc.getRenderItem().getItemModelMesher().register(ColourfulPortalsMod.enderPearlColouredReflective, 0, new ModelResourceLocation("colourfulportalsmod:colourful_ender_pearl_reflective", "inventory"));
	  
    //System.out.println("cp - Loading the rendering handler.");
    
    //RenderingRegistry.registerBlockHandler(ColourfulPortalsMod.standaloneRenderer);
  }
  
  public TextureAtlasSprite registerTexture(ResourceLocation location) {
	  return Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(location);
  }
  
  public File getSaveLocation()
    throws IOException
  {
    File saveDirectory = new File(Minecraft.getMinecraft().mcDataDir, "saves/" + MinecraftServer.getServer().getFolderName());
    saveDirectory.mkdir();
    
    return new File(saveDirectory, "colourful_portal_locations.dat");
  }
}