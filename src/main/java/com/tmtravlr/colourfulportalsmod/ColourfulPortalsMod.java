package com.tmtravlr.colourfulportalsmod;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeSet;

import scala.actors.threadpool.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IRegistry;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod(modid="colourfulportalsmod", name="Colourful Portals Mod", version="1.4.3")
public class ColourfulPortalsMod
{
	@Mod.Instance("colourfulPortalsMod")
	public static ColourfulPortalsMod colourfulPortalsMod;
	@SidedProxy(clientSide="com.tmtravlr.colourfulportalsmod.ClientProxy", serverSide="com.tmtravlr.colourfulportalsmod.CommonProxy")
	public static CommonProxy proxy;
	public static final Fluid colourfulFluid = new ColourfulFluid();
	public static Item bucketColourfulWaterEmpty;
	public static Item bucketColourfulWater;
	public static Item bucketColourfulWaterUnmixed;
	public static Item bucketColourfulWaterPartMixed;
	public static Item bucketColourfulWaterFirst;
	public static Item enderPearlColoured;
	public static Item enderPearlColouredReflective;
	public static Block colourfulWater;
	public static HashMap<Integer, BlockColourfulPortal> cpBlocks = new HashMap();
	public static HashMap<Integer, BlockStandaloneCP> scpBlocks = new HashMap();
	public static HashMap<Integer, Block> frameBlocks = new HashMap();
	private static HashMap<Integer, String> frameNames = new HashMap();
	private static final boolean debug = false;
	public static int colourfulPortalRenderId;
	public static int maxPortalGenerationDistance = 3000;
	public static int maxPortalsPerType = -1;
	public static int maxPortalSizeCheck = 16;
	public static int xpLevelMixingCost = 5;
	public static int xpLevelRemixingCost = 2;
	public static boolean useDestinationBlackList = true;
	public static boolean useDestinationWhiteList = false;
	public static boolean xpBottleCrafting = false;
	public static int[] destinationBlackList = { 1 };
	public static int[] destinationWhiteList = { 0, -1 };
	public static boolean useDimensionBlackList = false;
	public static boolean useDimensionWhiteList = false;
	public static int[] dimensionBlackList = new int[0];
	public static int[] dimensionWhiteList = { 0, 1, -1 };
	public static String[] frameBlockNames = { "wool", "stained_hardened_clay", "stained_glass" };
	private static String bucketColourfulWaterEmptyId = "bucket_colourful_water_empty";
	private static String bucketColourfulWaterId = "bucket_colourful_water";
	private static String bucketColourfulWaterUnmixedId = "bucket_colourful_water_unmixed";
	private static String bucketColourfulWaterPartMixedId = "bucket_colourful_water_part_mixed";
	private static String bucketColourfulWaterFirstId = "bucket_colourful_water_first";
	private static String colourfulEnderPearlId = "colourful_ender_pearl";
	private static String colourfulEnderPearlReflectiveId = "colourful_ender_pearl_reflective";
	private static String colourfulWaterId = "colourful_water";
	private static String colourfulPortalIdPrefix = "cp_";
	private static String standaloneCPIdPrefix = "scp_";
	public static CreativeTabs cpTab = new CreativeTabs("colourfulPortals")
	{
		public Item getTabIconItem()
		{
			return ColourfulPortalsMod.bucketColourfulWater;
		}
	};
	private static LinkedList<ColourfulPortalLocation> colourfulPortals = new LinkedList();
	private File saveLocation;
	private boolean loaded;
	public String currentFolder;

	public ColourfulPortalsMod()
	{
		this.loaded = false;
		this.currentFolder = "";
	}

	public static RenderStandaloneCP standaloneRenderer = new RenderStandaloneCP();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		colourfulPortalsMod = this;
		boolean addColourfulWaterToDungeonChests = true;

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();


		maxPortalGenerationDistance = config.get("other", "Random Portal Generation Max Distance", 3000).getInt();
		maxPortalsPerType = config.get("other", "Maximum Number of Portals per Type (Colour and Material)", -1).getInt();
		maxPortalSizeCheck = config.get("other", "Maximum Portal Size (Make Bigger for Larger Portals)", 16).getInt();
		xpLevelMixingCost = config.get("other", "Number of XP Levels Needed to Mix Colourful Water", 5).getInt();
		xpLevelRemixingCost = config.get("other", "Number of XP Levels Needed to Re-Mix Colourful Water", 2).getInt();
		xpBottleCrafting = config.get("other", "Allow crafting of colourful water with XP bottles (for automation)", false).getBoolean();
		addColourfulWaterToDungeonChests = config.getBoolean("Add Buckets of Colourful Water to Dungeon Chests?", "other", true, "If set to true, full and empty buckets of colourful water will occasionally spawn in chests.");
		if (xpLevelRemixingCost > xpLevelMixingCost) {
			xpLevelRemixingCost = xpLevelMixingCost;
		}
		config.addCustomCategoryComment("random_destination_blacklist", "If set to true, random destination portals with random dimensions\nwill not generate in any of the dimensions in this list. They can\nstill be created with same-dimension random destinations or placed\nmanually. Defaults to true. Takes precedence over the whitelist.");

		config.addCustomCategoryComment("random_destination_whitelist", "If set to true, random destination portals with random dimensions\nwill only generate in these dimensions. Ones with the same dimension\ncan still generate elsewhere. Defaults to false.");

		config.addCustomCategoryComment("dimension_blacklist", "If set to true, portals cannot be created in these dimensions\nat all, whether framed, single block, or 'random destination'\nportals. Takes precedence over the whitelist. Defaults to false.");

		config.addCustomCategoryComment("dimension_whitelist", "If set to true, portals can ONLY be created in the given dimensions,\nwhether framed, single block, or 'random destination' portals.\nDefaults to false.");

		int[] defaultBlackList = { 1 };
		int[] defaultWhiteList = { 0, -1 };
		int[] fullDefaultList = { 0, 1, -1 };
		int[] emptyList = new int[0];

		useDestinationBlackList = config.get("random_destination_blacklist", "Use this Blacklist?", true).getBoolean(true);
		useDestinationWhiteList = config.get("random_destination_whitelist", "Use this Whitelist?", false).getBoolean(false);
		destinationBlackList = config.get("random_destination_blacklist", "List of Blacklisted Dimensions for Random Generation", defaultBlackList).getIntList();
		destinationWhiteList = config.get("random_destination_whitelist", "List of Whitelisted Dimensions for Random Generation", defaultWhiteList).getIntList();
		
		useDimensionBlackList = config.get("dimension_blacklist", "Use this Blacklist?", false).getBoolean(false);
		useDimensionWhiteList = config.get("dimension_whitelist", "Use this Whitelist?", false).getBoolean(false);
		dimensionBlackList = config.get("dimension_blacklist", "List of Blacklisted Dimensions for all Portals", emptyList).getIntList();
		dimensionWhiteList = config.get("dimension_whitelist", "List of Whitelisted Dimensions for all Portals", fullDefaultList).getIntList();

		config.addCustomCategoryComment("portal_frame_types", "Blocks that can be used to make portals out of.\nThey should have 16 metadata types that represent\ncolours in the same way as wool.");

		String[] defaultPortalTypes = { "wool", "stained_hardened_clay", "stained_glass" };
		frameBlockNames = config.get("portal_frame_types", "Portal Frame Blocks", defaultPortalTypes).getStringList();
		bucketColourfulWaterEmpty = new ItemBucketColourfulWater(true, true, false).setUnlocalizedName("bucketColourfulWaterEmpty");
		bucketColourfulWater = new ItemBucketColourfulWater(true, true, true).setUnlocalizedName("bucketColourfulWater");
		bucketColourfulWaterUnmixed = new ItemBucketColourfulWater(false, false, true).setUnlocalizedName("bucketColourfulWaterUnmixed");
		bucketColourfulWaterPartMixed = new ItemBucketColourfulWater(true, false, true).setUnlocalizedName("bucketColourfulWaterPartMixed");
		bucketColourfulWaterFirst = new ItemBucketColourfulWater(false, true, false).setUnlocalizedName("bucketColourfulWaterFirst");
		enderPearlColoured = new ItemEnderPearlColoured(false);
		enderPearlColouredReflective = new ItemEnderPearlColoured(true);
		colourfulWater = new BlockColourfulWater().setDensity(colourfulFluid.getDensity()).setHardness(100.0F).setLightOpacity(3);
		
		config.save();

		GameRegistry.registerItem(bucketColourfulWaterEmpty, bucketColourfulWaterEmptyId);
		GameRegistry.registerItem(bucketColourfulWater, bucketColourfulWaterId);
		GameRegistry.registerItem(bucketColourfulWaterUnmixed, bucketColourfulWaterUnmixedId);
		GameRegistry.registerItem(bucketColourfulWaterPartMixed, bucketColourfulWaterPartMixedId);
		GameRegistry.registerItem(bucketColourfulWaterFirst, bucketColourfulWaterFirstId);
		GameRegistry.registerItem(enderPearlColoured, colourfulEnderPearlId);
		GameRegistry.registerItem(enderPearlColouredReflective, colourfulEnderPearlReflectiveId);

		GameRegistry.registerBlock(colourfulWater, colourfulWaterId);
		FluidContainerRegistry.registerFluidContainer(colourfulFluid, new ItemStack(bucketColourfulWater), new ItemStack(bucketColourfulWaterEmpty));


		BlockDispenser.dispenseBehaviorRegistry.putObject(bucketColourfulWaterEmpty, new BehaviorDefaultDispenseItem()
		{
			private final BehaviorDefaultDispenseItem field_150840_b = new BehaviorDefaultDispenseItem();
			private static final String __OBFID = "CL_00001400";

			public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
			{
				EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
				World world = blockSource.getWorld();
				int x = blockSource.getXInt() + enumfacing.getFrontOffsetX();
				int y = blockSource.getYInt() + enumfacing.getFrontOffsetY();
				int z = blockSource.getZInt() + enumfacing.getFrontOffsetZ();
				int meta = world.getBlockMetadata(x, y, z);
				Item item = itemStack.getItem();
				if ((world.getBlock(x, y, z) == ColourfulPortalsMod.colourfulWater) && (meta == 0)) {
					item = ColourfulPortalsMod.bucketColourfulWater;
				} else {
					return super.dispenseStack(blockSource, itemStack);
				}
				world.setBlockToAir(x, y, z);
				if (--itemStack.stackSize == 0)
				{
					itemStack.func_150996_a(item);
					itemStack.stackSize = 1;
				}
				else if (((TileEntityDispenser)blockSource.getBlockTileEntity()).func_146019_a(new ItemStack(item)) < 0)
				{
					this.field_150840_b.dispense(blockSource, new ItemStack(item));
				}
				return itemStack;
			}
		});
		BlockDispenser.dispenseBehaviorRegistry.putObject(bucketColourfulWater, new BehaviorDefaultDispenseItem()
		{
			private final BehaviorDefaultDispenseItem field_150840_b = new BehaviorDefaultDispenseItem();
			private static final String __OBFID = "CL_00001400";

			public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemStack)
			{
				EnumFacing enumfacing = BlockDispenser.func_149937_b(blockSource.getBlockMetadata());
				World world = blockSource.getWorld();
				int x = blockSource.getXInt() + enumfacing.getFrontOffsetX();
				int y = blockSource.getYInt() + enumfacing.getFrontOffsetY();
				int z = blockSource.getZInt() + enumfacing.getFrontOffsetZ();
				int meta = world.getBlockMetadata(x, y, z);
				Item item = itemStack.getItem();
				if ((world.getBlock(x, y, z) == Blocks.air) && (meta == 0)) {
					item = ColourfulPortalsMod.bucketColourfulWaterEmpty;
				} else {
					return super.dispenseStack(blockSource, itemStack);
				}
				world.setBlock(x, y, z, ColourfulPortalsMod.colourfulWater);
				if (--itemStack.stackSize == 0)
				{
					itemStack.func_150996_a(item);
					itemStack.stackSize = 1;
				}
				else if (((TileEntityDispenser)blockSource.getBlockTileEntity()).func_146019_a(new ItemStack(item)) < 0)
				{
					this.field_150840_b.dispense(blockSource, new ItemStack(item));
				}
				return itemStack;
			}
		});
		if (addColourfulWaterToDungeonChests)
		{
			ChestGenHooks.getInfo("pyramidDesertyChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 3));
			ChestGenHooks.getInfo("pyramidJungleChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 2));
			ChestGenHooks.getInfo("strongholdCrossing").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCorridor").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));
			ChestGenHooks.getInfo("dungeonChest").addItem(new WeightedRandomChestContent(bucketColourfulWaterEmpty, 0, 1, 1, 1));

			ChestGenHooks.getInfo("pyramidDesertyChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 2));
			ChestGenHooks.getInfo("pyramidJungleChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCrossing").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("strongholdCorridor").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
			ChestGenHooks.getInfo("dungeonChest").addItem(new WeightedRandomChestContent(bucketColourfulWater, 0, 1, 1, 1));
		}
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		for (int i = 0; i < frameBlockNames.length; i++)
		{
			Object blockObj = Block.blockRegistry.getObject(frameBlockNames[i]);
			
			if(blockObj == null || !(blockObj instanceof Block)) {
				System.err.println("[Colourful Portals] Error! Couldn't find a block with name '" + frameBlockNames[i] + "'!");
				continue;
			}
			
			Block frameBlock = (Block) blockObj;
			
			cpBlocks.put(i, (BlockColourfulPortal)new BlockColourfulPortal("portal_colour", Material.portal).setHardness(-1.0F).setStepSound(Block.soundTypeGlass).setLightLevel(0.75F).setBlockName("colourfulPortal"));
			scpBlocks.put(i, (BlockStandaloneCP)new BlockStandaloneCP("portal_colour", frameBlock.getMaterial()).setHardness(0.8F).setStepSound(frameBlock.stepSound).setLightLevel(0.75F).setBlockName("standaloneColourfulPortal"));
			frameBlocks.put(i, frameBlock);
			
			int colonIndex = frameBlockNames[i].indexOf(":");
			if (colonIndex != -1) {
				frameNames.put(i, frameBlockNames[i].substring(0, colonIndex) + "_" + frameBlockNames[i].substring(colonIndex + 1));
			}
			else {
				frameNames.put(i, frameBlockNames[i]);
			}
		}
		
		for (int i = 0; i < cpBlocks.size(); i++)
		{
			GameRegistry.registerBlock((Block)cpBlocks.get(i), colourfulPortalIdPrefix + frameNames.get(i));
			GameRegistry.registerBlock((Block)scpBlocks.get(i), ItemStandaloneCP.class, standaloneCPIdPrefix + frameNames.get(i));
		}

		
		for (int f = 0; f < frameBlocks.size(); f++) {
			for (int i = 0; i < 16; i++)
			{
				ItemStack frame = new ItemStack(frameBlocks.get(f), 1, i);
				ItemStack sCPStack = new ItemStack(scpBlocks.get(f), 1, i);

				GameRegistry.addRecipe(sCPStack, new Object[] { "WWW", "WBW", "WWW", Character.valueOf('W'), frame, Character.valueOf('B'), bucketColourfulWater });
			}
		}
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(bucketColourfulWaterPartMixed, 1), new Object[] { Items.water_bucket, bucketColourfulWaterEmpty, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(bucketColourfulWaterUnmixed, 1), new Object[] { Items.water_bucket, bucketColourfulWaterFirst, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(enderPearlColoured, 1), new Object[] { Items.ender_pearl, "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyeYellow", "dyeWhite" }));
		GameRegistry.addRecipe(new ItemStack(bucketColourfulWaterFirst, 1), new Object[] { "G", "B", Character.valueOf('G'), Items.gold_ingot, Character.valueOf('B'), Items.bucket });
		GameRegistry.addRecipe(new ItemStack(bucketColourfulWaterFirst, 1), new Object[] { "IGI", " I ", Character.valueOf('G'), Items.gold_ingot, Character.valueOf('I'), Items.iron_ingot });
		GameRegistry.addRecipe(new ItemStack(enderPearlColouredReflective, 1), new Object[] { " Q ", "QPQ", " Q ", Character.valueOf('Q'), Items.quartz, Character.valueOf('P'), enderPearlColoured });
		GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWaterFirst), new Object[] { new ItemStack(bucketColourfulWaterEmpty) });
		GameRegistry.addShapelessRecipe(new ItemStack(Items.bucket), new Object[] { new ItemStack(bucketColourfulWaterFirst) });
		
		if(xpBottleCrafting) {
			GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWater), new Object[] {new ItemStack(bucketColourfulWaterUnmixed), new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle)});
			GameRegistry.addShapelessRecipe(new ItemStack(bucketColourfulWater), new Object[] {new ItemStack(bucketColourfulWaterPartMixed), new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle),new ItemStack(Items.experience_bottle)});
		}

		colourfulPortalRenderId = RenderingRegistry.getNextAvailableRenderId();

		proxy.registerSounds();
		proxy.registerRenderers();
		proxy.registerEventHandlers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {}

	public static boolean isStandaloneCPBlock(Block block)
	{
		return scpBlocks.containsValue(block);
	}

	public static boolean isFramedCPBlock(Block block)
	{
		return cpBlocks.containsValue(block);
	}

	public static boolean isCPBlock(Block block)
	{
		return (isStandaloneCPBlock(block)) || (isFramedCPBlock(block));
	}

	public static boolean isFrameBlock(Block block)
	{
		return frameBlocks.containsValue(block);
	}

	public static boolean isPortalOrFrameBlock(IBlockAccess iba, int x, int y, int z)
	{
		return (isFramedCPBlock(iba.getBlock(x, y, z))) || (isFrameBlock(iba.getBlock(x, y, z)));
	}

	public static int getShiftedCPMetadata(IBlockAccess iba, int x, int y, int z)
	{
		Block block = iba.getBlock(x, y, z);
		int meta = iba.getBlockMetadata(x, y, z);
		return getShiftedCPMetadata(block, meta);
	}

	public static int getIndexFromShiftedMetadata(int meta)
	{
		return (int)Math.floor(meta / 16);
	}

	public static int getShiftedCPMetadata(Block block, int meta)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if ((cpBlocks.get(i) == block) || (scpBlocks.get(i) == block)) {
				return meta + 16 * i;
			}
		}
		return -1;
	}

	public static int getShiftedCPMetadataByFrameBlock(Block block, int meta)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == block) {
				return meta + 16 * i;
			}
		}
		return -1;
	}

	public static Block getCPBlockByShiftedMetadata(int meta)
	{
		return (Block)cpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getStandaloneCPBlockByShiftedMetadata(int meta)
	{
		return (Block)scpBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getFrameBlockByShiftedMetadata(int meta)
	{
		return frameBlocks.get(getIndexFromShiftedMetadata(meta));
	}

	public static Block getCPBlockByFrameBlock(Block frameBlock)
	{
		for (int i = 0; i < frameBlocks.size(); i++) {
			if (frameBlocks.get(i) == frameBlock) {
				return (Block)cpBlocks.get(i);
			}
		}
		return null;
	}

	public static int unshiftCPMetadata(int meta)
	{
		return meta % 16;
	}

	public static void setFramedCPBlock(World world, int x, int y, int z, Block frameBlock, int meta, int flag)
	{
		int index = getIndexFromShiftedMetadata(getShiftedCPMetadataByFrameBlock(frameBlock, meta));
		Block block = (Block)cpBlocks.get(index);
		world.setBlock(x, y, z, block, meta, flag);
	}

	public static boolean isDimensionValidForDestination(int dimension)
	{
		if (!isDimensionValidAtAll(dimension)) {
			return false;
		}
		if (useDestinationWhiteList)
		{
			if (destinationWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int i = 0; i < destinationWhiteList.length; i++) {
				if (dimension == destinationWhiteList[i]) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDestinationBlackList) {
			for (int i = 0; i < destinationBlackList.length; i++) {
				if (dimension == destinationBlackList[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isDimensionValidAtAll(int dimension)
	{
		if (useDimensionWhiteList)
		{
			if (dimensionWhiteList.length == 0) {
				return false;
			}
			boolean inWhiteList = false;
			for (int i = 0; i < dimensionWhiteList.length; i++) {
				if (dimension == dimensionWhiteList[i]) {
					inWhiteList = true;
				}
			}
			if (!inWhiteList) {
				return false;
			}
		}
		if (useDimensionBlackList) {
			for (int i = 0; i < dimensionBlackList.length; i++) {
				if (dimension == dimensionBlackList[i]) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean canCreatePortal(World world, int x, int y, int z, Block block, int meta)
	{
		if (tooManyPortals(block, meta)) {
			return false;
		}
		if (!isDimensionValidAtAll(world.provider.dimensionId)) {
			return false;
		}
		return true;
	}

	public static boolean tooManyPortals(Block block, int meta)
	{
		if (maxPortalsPerType < 0) {
			return false;
		}
		if (maxPortalsPerType == 0) {
			return true;
		}
		int portalsWithType = 0;
		for (int i = 0; i < colourfulPortals.size(); i++) {
			if (((ColourfulPortalLocation)colourfulPortals.get(i)).portalMetadata == getShiftedCPMetadata(block, meta)) {
				portalsWithType++;
			}
		}
		if (portalsWithType >= maxPortalsPerType) {
			return true;
		}
		return false;
	}

	public void loadPortalsList()
	{
		FileInputStream fInput = null;
		ObjectInputStream oInput = null;
		try
		{
			this.saveLocation = proxy.getSaveLocation();
			if (this.saveLocation.exists())
			{
				fInput = new FileInputStream(this.saveLocation);
				oInput = new ObjectInputStream(fInput);
				colourfulPortals = (LinkedList)oInput.readObject();
				oInput.close();
				fInput.close();

				checkForPortalChanges();
			}
			else
			{
				this.saveLocation.createNewFile();





				colourfulPortals = new LinkedList();
			}
		}
		catch (Exception e)
		{
			if (!(e instanceof EOFException)) {
				e.printStackTrace();
			}
			try
			{
				if (oInput != null) {
					oInput.close();
				}
				if (fInput != null) {
					fInput.close();
				}
			}
			catch (IOException ioe) {}
		}
	}

	private void checkForPortalChanges()
	{
		ArrayList<ColourfulPortalLocation> toDelete = new ArrayList();
		for (ColourfulPortalLocation portal : colourfulPortals)
		{
			WorldServer currentWS = MinecraftServer.getServer().worldServerForDimension(portal.dimension);
			if ((getCPBlockByShiftedMetadata(portal.portalMetadata) != currentWS.getBlock(portal.xPos, portal.yPos, portal.zPos)) && (getStandaloneCPBlockByShiftedMetadata(portal.portalMetadata) != currentWS.getBlock(portal.xPos, portal.yPos, portal.zPos))) {
				if (!BlockColourfulPortal.tryToCreatePortal(currentWS, portal.xPos, portal.yPos, portal.zPos, false)) {
					toDelete.add(portal);
				}
			}
		}
		for (ColourfulPortalLocation deleted : toDelete) {
			colourfulPortals.remove(deleted);
		}
		savePortals();
	}

	public static ColourfulPortalLocation getColourfulDestination(World world, int x, int y, int z)
	{
		if (colourfulPortals.size() > 0)
		{
			ColourfulPortalLocation start = findCPLocation(world, x, y, z);


			int originalPos = colourfulPortals.indexOf(start);
			if (originalPos == -1) {
				return new ColourfulPortalLocation(x, y, z, world.provider.dimensionId, getShiftedCPMetadata(world, x, y, z));
			}
			int size = colourfulPortals.size();
			for (int i = 0; i < size; i++)
			{
				int index = i + originalPos + 1;
				if (index >= size) {
					index -= size;
				}
				ColourfulPortalLocation current = (ColourfulPortalLocation)colourfulPortals.get(index);
				if (current.portalMetadata == start.portalMetadata) {
					if(MinecraftServer.getServer().worldServerForDimension(current.dimension) != null) {
						return current;
					}
				}
			}
			return start;
		}
		return new ColourfulPortalLocation(x, y, z, world.provider.dimensionId, getShiftedCPMetadata(world, x, y, z));
	}

	public static ColourfulPortalLocation findCPLocation(World world, int x, int y, int z)
	{
		if (!isCPBlock(world.getBlock(x, y, z))) {
			return null;
		}
		if (isStandaloneCPBlock(world.getBlock(x, y, z))) {
			return new ColourfulPortalLocation(x, y, z, world.provider.dimensionId, getShiftedCPMetadata(world, x, y, z));
		}
		boolean xDir = true;
		boolean yDir = true;
		boolean zDir = true;
		int i = 0;
		int maxSize = maxPortalSizeCheck * maxPortalSizeCheck + 1;
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x + i, y, z))); i++) {}
		if (!isFrameBlock(world.getBlock(x + i, y, z)))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x - i, y, z))); i++) {}
		if (!isFrameBlock(world.getBlock(x - i, y, z)))
		{
			zDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x, y + i, z))); i++) {}
		if (!isFrameBlock(world.getBlock(x, y + i, z)))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x, y - i, z))); i++) {}
		if (!isFrameBlock(world.getBlock(x, y - i, z)))
		{
			zDir = false;
			xDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x, y, z + i))); i++) {}
		if (!isFrameBlock(world.getBlock(x, y, z + i)))
		{
			xDir = false;
			yDir = false;
		}
		for (i = 0; (i < maxSize) && (isCPBlock(world.getBlock(x, y, z - i))); i++) {}
		if (!isFrameBlock(world.getBlock(x, y, z - i)))
		{
			xDir = false;
			yDir = false;
		}
		if ((!xDir) && (!yDir) && (!zDir)) {
			return null;
		}
		CPLSet visited = new CPLSet();
		Stack<ColourfulPortalLocation> toVisit = new Stack();

		toVisit.push(new ColourfulPortalLocation(x, y, z, world.provider.dimensionId, getShiftedCPMetadata(world, x, y, z)));

		visited.add(toVisit.peek());
		while (!toVisit.empty())
		{
			ColourfulPortalLocation current = (ColourfulPortalLocation)toVisit.pop();
			if (colourfulPortals.contains(current)) {
				return current;
			}
			if ((zDir) || (xDir))
			{
				if (isCPBlock(world.getBlock(current.xPos, current.yPos + 1, current.zPos)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos, current.yPos + 1, current.zPos, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos, current.yPos + 1, current.zPos));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlock(current.xPos, current.yPos - 1, current.zPos)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos, current.yPos - 1, current.zPos, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos, current.yPos - 1, current.zPos));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
			if ((zDir) || (yDir))
			{
				if (isCPBlock(world.getBlock(current.xPos + 1, current.yPos, current.zPos)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos + 1, current.yPos, current.zPos, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos + 1, current.yPos, current.zPos));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlock(current.xPos - 1, current.yPos, current.zPos)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos - 1, current.yPos, current.zPos, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos - 1, current.yPos, current.zPos));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
			if ((yDir) || (xDir))
			{
				if (isCPBlock(world.getBlock(current.xPos, current.yPos, current.zPos + 1)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos, current.yPos, current.zPos + 1, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos, current.yPos, current.zPos + 1));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
				if (isCPBlock(world.getBlock(current.xPos, current.yPos, current.zPos - 1)))
				{
					ColourfulPortalLocation temp = new ColourfulPortalLocation(current.xPos, current.yPos, current.zPos - 1, world.provider.dimensionId, getShiftedCPMetadata(world, current.xPos, current.yPos, current.zPos - 1));
					if (!visited.contains(temp))
					{
						toVisit.push(temp);
						visited.add(temp);
					}
				}
			}
		}
		return null;
	}

	public static void deletePortal(ColourfulPortalLocation locToDelete)
	{
		if (colourfulPortals.remove(locToDelete)) {
			savePortals();
		}
	}

	public static boolean addPortalToList(ColourfulPortalLocation newLocation)
	{
		if (!colourfulPortals.contains(newLocation))
		{
			colourfulPortals.add(newLocation);





			savePortals();
			return true;
		}
		return false;
	}

	private static void savePortals()
	{
		try
		{
			FileOutputStream fOut = new FileOutputStream(colourfulPortalsMod.saveLocation);
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			oOut.writeObject(colourfulPortals);
			oOut.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class CPLSet
	extends TreeSet<ColourfulPortalsMod.ColourfulPortalLocation>
	{
		public CPLSet()
		{
			super(CPLcomparator);
		}
	}

	public static Comparator<ColourfulPortalLocation> CPLcomparator = new Comparator<ColourfulPortalLocation>()
			{

		@Override
		public int compare(ColourfulPortalsMod.ColourfulPortalLocation first, ColourfulPortalsMod.ColourfulPortalLocation second)
		{
			if (first.portalMetadata != second.portalMetadata) {
				return second.portalMetadata - first.portalMetadata;
			}
			if (first.dimension != second.dimension) {
				return second.dimension - first.dimension;
			}
			if (first.xPos != second.xPos) {
				return second.xPos - first.xPos;
			}
			if (first.yPos != second.yPos) {
				return second.yPos - first.yPos;
			}
			if (first.zPos != second.zPos) {
				return second.zPos - first.zPos;
			}
			return 0;
		}
			};

			public static class ColourfulPortalLocation
			implements Serializable
			{
				int xPos;
				int yPos;
				int zPos;
				int dimension;
				int portalMetadata;

				public ColourfulPortalLocation(int x, int y, int z, int dim, int meta)
				{
					this.xPos = x;
					this.yPos = y;
					this.zPos = z;
					this.dimension = dim;
					this.portalMetadata = meta;
				}

				public boolean equals(Object o)
				{
					if ((o == null) || (!(o instanceof ColourfulPortalLocation))) {
						return false;
					}
					ColourfulPortalLocation other = (ColourfulPortalLocation)o;
					return (this.xPos == other.xPos) && (this.yPos == other.yPos) && (this.zPos == other.zPos) && (this.dimension == other.dimension) && (this.portalMetadata == other.portalMetadata);
				}

				public String toString()
				{
					return "CPL[meta=" + this.portalMetadata + ", x=" + this.xPos + ", y=" + this.yPos + ", z=" + this.zPos + ", dim=" + this.dimension + "]";
				}
			}
}