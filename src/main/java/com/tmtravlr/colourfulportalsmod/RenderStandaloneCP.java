package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class RenderStandaloneCP
implements ISimpleBlockRenderingHandler
{
	private static double inSet = 0.09D;
	private static double xMin;
	private static double xMax;
	private static double yMin;
	private static double yMax;
	private static double zMin;
	private static double zMax;
	private static double uMin;
	private static double uMax;
	private static double vMin;
	private static double vMax;
	private static final float LIGHT_Y_NEG = 0.5F;
	private static final float LIGHT_Y_POS = 1.0F;
	private static final float LIGHT_X = 0.6F;
	private static final float LIGHT_Z = 0.8F;

	public void renderInventoryBlock(Block block, int meta, int modelId, RenderBlocks rb)
	{
		Tessellator.instance.startDrawingQuads();

		renderMainBlock(0, 0, 0, block, rb, 16777215, meta);

		Tessellator.instance.draw();
	}

	public boolean renderWorldBlock(IBlockAccess iba, int x, int y, int z, Block block, int modelId, RenderBlocks rb)
	{
		Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(iba, x, y, z));

		int meta = iba.getBlockMetadata(x, y, z);
		int color = block.colorMultiplier(iba, x, y, z);

		Block frameBlock = ColourfulPortalsMod.frameBlocks.get(ColourfulPortalsMod.getIndexFromShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(block, meta)));
		if (ClientProxy.renderPass != 0)
		{
			renderPortal(x, y, z, block, rb);
			if (!frameBlock.isOpaqueCube()) {
				renderMainBlock(x, y, z, block, rb, color, meta);
			}
		}
		else
		{
			renderMainBlock(x, y, z, block, rb, color, meta);
		}
		return true;
	}

	private boolean renderMainBlock(int x, int y, int z, Block block, RenderBlocks rb, int color, int meta)
	{
		if (ColourfulPortalsMod.isStandaloneCPBlock(block))
		{
			Tessellator tess = Tessellator.instance;
			Block frameBlock = ColourfulPortalsMod.frameBlocks.get(ColourfulPortalsMod.getIndexFromShiftedMetadata(ColourfulPortalsMod.getShiftedCPMetadata(block, meta)));
			IIcon blockIcon = frameBlock.getIcon(0, meta);

			if(color == 16777215) {
				//try to get the render colour instead if there's no color
				color = frameBlock.getRenderColor(meta);
			}


			if(blockIcon == null) {
				blockIcon = Blocks.stone.getIcon(0, 0);
			}


			float red = (color >> 16 & 0xFF) / 255.0F;
			float green = (color >> 8 & 0xFF) / 255.0F;
			float blue = (color & 0xFF) / 255.0F;

			rb.setOverrideBlockTexture(blockIcon);

			xMin = x + rb.renderMinX;
			xMax = x + rb.renderMaxX;
			yMin = y + rb.renderMinY;
			yMax = y + 0.8D;
			zMin = z + rb.renderMinZ;
			zMax = z + rb.renderMaxZ;

			uMin = rb.overrideBlockTexture.getInterpolatedU(rb.renderMinX * 16.0D);
			uMax = rb.overrideBlockTexture.getInterpolatedU(rb.renderMaxX * 16.0D);
			vMin = rb.overrideBlockTexture.getInterpolatedV(rb.renderMinZ * 16.0D);
			vMax = rb.overrideBlockTexture.getInterpolatedV(rb.renderMaxZ * 16.0D);


			tess.setColorOpaque_F(0.5F * red, 0.5F * green, 0.5F * blue);
			tess.setNormal(0.0F, -1.0F, 0.0F);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), p(0.8D, vMin, vMax));


			tess.setColorOpaque_F(0.6F * red, 0.6F * green, 0.6F * blue);

			tess.setNormal(-1.0F, 0.0F, 0.0F);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMax, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMax, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), vMin);

			tess.setNormal(1.0F, 0.0F, 0.0F);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMax, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMax, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), p(0.8D, vMin, vMax));


			tess.setColorOpaque_F(0.8F * red, 0.8F * green, 0.8F * blue);

			tess.setNormal(0.0F, 0.0F, -1.0F);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.05D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMax, p(0.95D, zMin, zMax), p(0.95D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMax, p(0.95D, zMin, zMax), p(0.05D, uMin, uMax), p(0.8D, vMin, vMax));

			tess.setNormal(0.0F, 0.0F, 1.0F);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), vMin);
			tess.addVertexWithUV(p(0.05D, xMin, xMax), yMax, p(0.05D, zMin, zMax), p(0.05D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMax, p(0.05D, zMin, zMax), p(0.95D, uMin, uMax), p(0.8D, vMin, vMax));
			tess.addVertexWithUV(p(0.95D, xMin, xMax), yMin, p(0.05D, zMin, zMax), p(0.95D, uMin, uMax), vMin);

			tess.setColorOpaque_F(red, green, blue);

			double s = 0.05D;
			double e = 0.95D;

			tess.setNormal(0.0F, 1.0F, 0.0F);


			renderTopFace(tess, s, s, s, 0.25D, 0.25D, 0.25D, 0.25D, s);
			renderTopFace(tess, 0.25D, s, 0.25D, 0.25D, 0.5D, 0.25D - inSet, 0.5D, s);
			renderTopFace(tess, 0.5D, s, 0.5D, 0.25D - inSet, 0.75D, 0.25D, 0.75D, s);
			renderTopFace(tess, 0.75D, s, 0.75D, 0.25D, e, 0.25D, e, s);
			renderTopFace(tess, s, 0.25D, s, 0.5D, 0.25D - inSet, 0.5D, 0.25D, 0.25D);
			renderTopFace(tess, s, 0.5D, s, 0.75D, 0.25D, 0.75D, 0.25D - inSet, 0.5D);
			renderTopFace(tess, 0.75D, 0.25D, 0.75D + inSet, 0.5D, e, 0.5D, e, 0.25D);
			renderTopFace(tess, 0.75D + inSet, 0.5D, 0.75D, 0.75D, e, 0.75D, e, 0.5D);
			renderTopFace(tess, s, 0.75D, s, e, 0.25D, e, 0.25D, 0.75D);
			renderTopFace(tess, 0.25D, 0.75D, 0.25D, e, 0.5D, e, 0.5D, 0.75D + inSet);
			renderTopFace(tess, 0.5D, 0.75D + inSet, 0.5D, e, 0.75D, e, 0.75D, 0.75D);
			renderTopFace(tess, 0.75D, 0.75D, 0.75D, e, e, e, e, 0.75D);

			//Render the "bucket" part
			
			tess.setColorOpaque_F(255, 255, 255);
			
			rb.setOverrideBlockTexture(Blocks.iron_block.getIcon(0, 0));

			uMin = rb.overrideBlockTexture.getInterpolatedU(rb.renderMinX * 16.0D);
			uMax = rb.overrideBlockTexture.getInterpolatedU(rb.renderMaxX * 16.0D);
			vMin = rb.overrideBlockTexture.getInterpolatedV(rb.renderMinZ * 16.0D);
			vMax = rb.overrideBlockTexture.getInterpolatedV(rb.renderMaxZ * 16.0D);

			renderOctagon(tess, 0.2D, 1.0D);


			rb.setOverrideBlockTexture(Blocks.gold_block.getIcon(0, 0));

			uMin = rb.overrideBlockTexture.getInterpolatedU(rb.renderMinX * 16.0D);
			uMax = rb.overrideBlockTexture.getInterpolatedU(rb.renderMaxX * 16.0D);
			vMin = rb.overrideBlockTexture.getInterpolatedV(rb.renderMinZ * 16.0D);
			vMax = rb.overrideBlockTexture.getInterpolatedV(rb.renderMaxZ * 16.0D);

			renderOctagon(tess, 0.3D, 0.75D);


			rb.clearOverrideBlockTexture();

			return true;
		}
		return false;
	}

	private boolean renderPortal(int x, int y, int z, Block block, RenderBlocks rb)
	{
		Tessellator tess = Tessellator.instance;

		rb.setOverrideBlockTexture(BlockColourfulPortal.instance.getIcon(0, 0));

		xMin = x + rb.renderMinX;
		xMax = x + rb.renderMaxX;
		yMin = y + rb.renderMinY;
		yMax = y + 0.8D;
		zMin = z + rb.renderMinZ;
		zMax = z + rb.renderMaxZ;

		uMin = block.getIcon(0, 0).getInterpolatedU(rb.renderMinX * 16.0D);
		uMax = block.getIcon(0, 0).getInterpolatedU(rb.renderMaxX * 16.0D);
		vMin = block.getIcon(0, 0).getInterpolatedV(rb.renderMinZ * 16.0D);
		vMax = block.getIcon(0, 0).getInterpolatedV(rb.renderMaxZ * 16.0D);
		for (int i = 0; i < 8; i++)
		{
			double u1 = 0.0D;double u2 = 0.0D;double v1 = 0.0D;double v2 = 0.0D;
			if (i == 0)
			{
				u1 = 0.0D;v1 = 0.5D;
				u2 = 0.0D;v2 = 1.0D;
			}
			if (i == 1)
			{
				u1 = 0.5D;v1 = 1.0D;
				u2 = 1.0D;v2 = 1.0D;
			}
			if (i == 2)
			{
				u1 = 1.0D;v1 = 0.5D;
				u2 = 1.0D;v2 = 0.0D;
			}
			if (i == 3)
			{
				u1 = 0.5D;v1 = 0.0D;
				u2 = 0.0D;v2 = 0.0D;
			}
			if (i == 4)
			{
				u1 = 0.0D;v1 = 1.0D;
				u2 = 0.5D;v2 = 1.0D;
			}
			if (i == 5)
			{
				u1 = 1.0D;v1 = 1.0D;
				u2 = 1.0D;v2 = 0.5D;
			}
			if (i == 6)
			{
				u1 = 1.0D;v1 = 0.0D;
				u2 = 0.5D;v2 = 0.0D;
			}
			if (i == 7)
			{
				u1 = 0.0D;v1 = 0.0D;
				u2 = 0.0D;v2 = 0.5D;
			}
			tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
			tess.addVertexWithUV(p(0.5D, xMin, xMax), p(1.2D, yMin, yMax), p(0.5D, zMin, zMax), p(0.5D, uMin, uMax), p(0.5D, vMin, vMax));
			tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
			tess.addVertexWithUV(p(u1, xMin, xMax), yMax, p(v1, zMin, zMax), p(u1, uMin, uMax), p(v1, vMin, vMax));
			tess.addVertexWithUV(p(u2, xMin, xMax), yMax, p(v2, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
			tess.addVertexWithUV(p(u2, xMin, xMax), yMax, p(v2, zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
		}
		tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
		tess.addVertexWithUV(xMax, yMax, zMin, uMin, p(0.8D, vMin, vMax));
		tess.addVertexWithUV(xMax, yMax, zMax, uMax, p(0.8D, vMin, vMax));
		tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.1F);
		tess.addVertexWithUV(xMax, yMin, zMax, uMax, vMin);
		tess.addVertexWithUV(xMax, yMin, zMin, uMin, vMin);

		tess.addVertexWithUV(xMin, yMin, zMin, uMin, vMin);
		tess.addVertexWithUV(xMin, yMin, zMax, uMax, vMin);
		tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
		tess.addVertexWithUV(xMin, yMax, zMax, uMax, p(0.8D, vMin, vMax));
		tess.addVertexWithUV(xMin, yMax, zMin, uMin, p(0.8D, vMin, vMax));


		tess.addVertexWithUV(xMax, yMax, zMax, uMax, p(0.8D, vMin, vMax));
		tess.addVertexWithUV(xMin, yMax, zMax, uMin, p(0.8D, vMin, vMax));
		tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.1F);
		tess.addVertexWithUV(xMin, yMin, zMax, uMin, vMin);
		tess.addVertexWithUV(xMax, yMin, zMax, uMax, vMin);

		tess.addVertexWithUV(xMax, yMin, zMin, uMax, vMin);
		tess.addVertexWithUV(xMin, yMin, zMin, uMin, vMin);
		tess.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.6F);
		tess.addVertexWithUV(xMin, yMax, zMin, uMin, p(0.8D, vMin, vMax));
		tess.addVertexWithUV(xMax, yMax, zMin, uMax, p(0.8D, vMin, vMax));

		rb.clearOverrideBlockTexture();

		return true;
	}

	private double p(double percent, double min, double max)
	{
		return (max - min) * percent + min;
	}

	private void renderOctagon(Tessellator tess, double depth, double scale)
	{
		for (int i = 0; i < 8; i++)
		{
			double x1 = 0.0D;double x2 = 0.0D;double z1 = 0.0D;double z2 = 0.0D;
			double u1 = 0.0D;double u2 = 0.0D;double v1 = 0.0D;double v2 = 0.0D;
			if (i == 0)
			{
				x1 = 0.25D - inSet;z1 = 0.5D;
				x2 = 0.25D;z2 = 0.75D;
				u1 = 0.0D;v1 = 0.5D;
				u2 = 0.0D;v2 = 1.0D;
			}
			if (i == 1)
			{
				x1 = 0.5D;z1 = 0.75D + inSet;
				x2 = 0.75D;z2 = 0.75D;
				u1 = 0.5D;v1 = 1.0D;
				u2 = 1.0D;v2 = 1.0D;
			}
			if (i == 2)
			{
				x1 = 0.75D + inSet;z1 = 0.5D;
				x2 = 0.75D;z2 = 0.25D;
				u1 = 1.0D;v1 = 0.5D;
				u2 = 1.0D;v2 = 0.0D;
			}
			if (i == 3)
			{
				x1 = 0.5D;z1 = 0.25D - inSet;
				x2 = 0.25D;z2 = 0.25D;
				u1 = 0.5D;v1 = 0.0D;
				u2 = 0.0D;v2 = 0.0D;
			}
			if (i == 4)
			{
				x1 = 0.25D;z1 = 0.75D;
				x2 = 0.5D;z2 = 0.75D + inSet;
				u1 = 0.0D;v1 = 1.0D;
				u2 = 0.5D;v2 = 1.0D;
			}
			if (i == 5)
			{
				x1 = 0.75D;z1 = 0.75D;
				x2 = 0.75D + inSet;z2 = 0.5D;
				u1 = 1.0D;v1 = 1.0D;
				u2 = 1.0D;v2 = 0.5D;
			}
			if (i == 6)
			{
				x1 = 0.75D;z1 = 0.25D;
				x2 = 0.5D;z2 = 0.25D - inSet;
				u1 = 1.0D;v1 = 0.0D;
				u2 = 0.5D;v2 = 0.0D;
			}
			if (i == 7)
			{
				x1 = 0.25D;z1 = 0.25D;
				x2 = 0.25D - inSet;z2 = 0.5D;
				u1 = 0.0D;v1 = 0.0D;
				u2 = 0.0D;v2 = 0.5D;
			}
			tess.addVertexWithUV(p(0.5D, xMin, xMax), p(depth, yMin, yMax), p(0.5D, zMin, zMax), p(0.5D, uMin, uMax), p(0.5D, vMin, vMax));
			tess.addVertexWithUV(p(x1 * scale + 0.5D * (1.0D - scale), xMin, xMax), p(1.0D * scale + 0.5D * (1.0D - scale), yMin, yMax), p(z1 * scale + 0.5D * (1.0D - scale), zMin, zMax), p(u1, uMin, uMax), p(v1, vMin, vMax));
			tess.addVertexWithUV(p(x2 * scale + 0.5D * (1.0D - scale), xMin, xMax), p(1.0D * scale + 0.5D * (1.0D - scale), yMin, yMax), p(z2 * scale + 0.5D * (1.0D - scale), zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
			tess.addVertexWithUV(p(x2 * scale + 0.5D * (1.0D - scale), xMin, xMax), p(1.0D * scale + 0.5D * (1.0D - scale), yMin, yMax), p(z2 * scale + 0.5D * (1.0D - scale), zMin, zMax), p(u2, uMin, uMax), p(v2, vMin, vMax));
		}
	}

	private void renderTopFace(Tessellator tess, double xMin1, double zMin1, double xMin2, double zMax2, double xMax3, double zMax3, double xMax4, double zMin4)
	{
		tess.addVertexWithUV(p(xMin1, xMin, xMax), yMax, p(zMin1, zMin, zMax), p(xMin1, uMin, uMax), p(zMin1, vMin, vMax));
		tess.addVertexWithUV(p(xMin2, xMin, xMax), yMax, p(zMax2, zMin, zMax), p(xMin2, uMin, uMax), p(zMax2, vMin, vMax));
		tess.addVertexWithUV(p(xMax3, xMin, xMax), yMax, p(zMax3, zMin, zMax), p(xMax3, uMin, uMax), p(zMax3, vMin, vMax));
		tess.addVertexWithUV(p(xMax4, xMin, xMax), yMax, p(zMin4, zMin, zMax), p(xMax4, uMin, uMax), p(zMin4, vMin, vMax));
	}

	public int getRenderId()
	{
		return ColourfulPortalsMod.colourfulPortalRenderId;
	}

	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}