package com.tmtravlr.colourfulportalsmod;

import net.minecraft.item.EnumRarity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ColourfulFluid
  extends Fluid
{
  public ColourfulFluid()
  {
    super("colourful_fluid");
    this.setUnlocalizedName("ColourfulWater");
    
    setLuminosity(5);
    setRarity(EnumRarity.rare);
    FluidRegistry.registerFluid(this);
  }
}