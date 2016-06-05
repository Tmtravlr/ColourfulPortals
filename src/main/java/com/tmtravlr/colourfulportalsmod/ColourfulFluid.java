package com.tmtravlr.colourfulportalsmod;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ColourfulFluid
extends Fluid {
	
	public ColourfulFluid() {
		
		super("colourful_fluid", new ResourceLocation("colourfulportalsmod:blocks/colourful_water"), new ResourceLocation("colourfulportalsmod:blocks/colourful_water_flow"));
		this.setUnlocalizedName("ColourfulWater");

		setLuminosity(5);
		setRarity(EnumRarity.RARE);
		FluidRegistry.registerFluid(this);
	}
}