package com.tmtravlr.colourfulportalsmod;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class ColourfulTeleporter
  extends Teleporter
{
  private WorldServer worldServer;
  private double x;
  private double y;
  private double z;
  private static final boolean debug = false;
  
  public ColourfulTeleporter(WorldServer worldServerOld, double xToSet, double yToSet, double zToSet)
  {
    super(worldServerOld);
    this.worldServer = worldServerOld;
    this.x = xToSet;
    this.y = yToSet;
    this.z = zToSet;
  }
  
  public void placeInPortal(Entity entity, float par8)
  {
    entity.setLocationAndAngles(this.x, this.y, this.z, entity.rotationYaw, 0.0F);
    entity.motionX = (entity.motionY = entity.motionZ = 0.0D);
    entity.setSneaking(false);
  }
}