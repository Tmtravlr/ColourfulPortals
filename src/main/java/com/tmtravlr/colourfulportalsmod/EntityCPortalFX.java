package com.tmtravlr.colourfulportalsmod;

import java.util.Random;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class EntityCPortalFX
  extends EntityFX
{
  private float portalParticleScale;
  private double portalPosX;
  private double portalPosY;
  private double portalPosZ;
  
  public EntityCPortalFX(World world, double x, double y, double z, double xVel, double yVel, double zVel)
  {
    this(world, x, y, z, xVel, yVel, zVel, false);
  }
  
  public EntityCPortalFX(World world, double x, double y, double z, double xVel, double yVel, double zVel, boolean large)
  {
    super(world, x, y, z, xVel, yVel, zVel);
    this.motionX = xVel;
    this.motionY = yVel;
    this.motionZ = zVel;
    this.portalPosX = (this.posX = x);
    this.portalPosY = (this.posY = y);
    this.portalPosZ = (this.posZ = z);
    if (large) {
      this.portalParticleScale = (this.particleScale = 2.0F);
    } else {
      this.portalParticleScale = (this.particleScale = this.rand.nextFloat() * 0.2F + 0.5F);
    }
    this.particleBlue = (this.rand.nextFloat() * 0.7F + 0.3F);
    this.particleGreen = (this.rand.nextFloat() * 0.5F);
    this.particleRed = (this.rand.nextFloat() * 0.7F + 0.3F);
    this.particleMaxAge = ((int)(Math.random() * 10.0D) + 40);
    this.noClip = true;
    setParticleTextureIndex((int)(Math.random() * 8.0D));
  }
  
  @Override
  public void renderParticle(WorldRenderer renderer, Entity entity, float par2, float par3, float par4, float par5, float par6, float par7)
  {
    float scaleFactor = (this.particleAge + par2) / this.particleMaxAge;
    scaleFactor = 1.0F - scaleFactor;
    scaleFactor *= scaleFactor;
    scaleFactor = 1.0F - scaleFactor;
    this.particleScale = (this.portalParticleScale * scaleFactor);
    super.renderParticle(renderer, entity, par2, par3, par4, par5, par6, par7);
  }
  
  public int getBrightnessForRender(float par1)
  {
    int i = super.getBrightnessForRender(par1);
    float f1 = this.particleAge / this.particleMaxAge;
    f1 *= f1;
    f1 *= f1;
    int j = i & 0xFF;
    int k = i >> 16 & 0xFF;
    k += (int)(f1 * 15.0F * 16.0F);
    if (k > 240) {
      k = 240;
    }
    return j | k << 16;
  }
  
  public float getBrightness(float par1)
  {
    float f1 = super.getBrightness(par1);
    float f2 = this.particleAge / this.particleMaxAge;
    f2 = f2 * f2 * f2 * f2;
    return f1 * (1.0F - f2) + f2;
  }
  
  public void onUpdate()
  {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    float f = this.particleAge / this.particleMaxAge;
    float f1 = f;
    f = -f + f * f * 2.0F;
    f = 1.0F - f;
    this.posX = (this.portalPosX + this.motionX * f);
    this.posY = (this.portalPosY + this.motionY * f + (1.0F - f1));
    this.posZ = (this.portalPosZ + this.motionZ * f);
    if (this.particleAge++ >= this.particleMaxAge) {
      setDead();
    }
  }
}