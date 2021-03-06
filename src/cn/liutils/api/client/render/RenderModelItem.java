package cn.liutils.api.client.render;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import cn.liutils.api.client.model.IItemModel;
import cn.liutils.api.client.util.RenderUtils;

/**
 * Item model render. Implemented lots of methods to adjust rendering and positioning.
 */
public class RenderModelItem implements IItemRenderer {

	protected Tessellator t = Tessellator.instance;
	protected Minecraft mc = Minecraft.getMinecraft();
	protected static Random RNG = new Random();
	
	/**
	 * The target rendering model.
	 */
	IItemModel model;
	
	/**
	 * Default binding texture.
	 */
	ResourceLocation texturePath;
	
	/**
	 * About TRANSFORMATION:
	 * The transform process goes like : Standard-> Sub
	 * Within one transform goes like : Scale -> Rotation -> Transformation
	 * For example, when rendering EQUIPPING:
	 * StdScale->Std Rotation->Std Offset->Equip Scale->Equip Rotation->Equip Offset
	 */
	
	/**
	 * Standard Rotation.
	 */
	public Vec3 stdRotation = initVec();
	
	/**
	 * Standard Offset.
	 */
	public Vec3 stdOffset = initVec();
	
	/**
	 * Standard Scale.
	 */
	public double scale = 1.0F;
	
	/**
	 * Equip Offset
	 */
	public Vec3 equipOffset = initVec();
	
	/**
	 * Equip Rotation
	 */
	public Vec3 equipRotation = initVec();
	
	/**
	 * Inventory Scale
	 */
	public double invScale = 1.0F;
	
	/**
	 * Inventory Offset
	 */
	public Vector2f invOffset = new Vector2f();
	
	/**
	 * Inventory Rotation
	 */
	public Vec3 invRotation = initVec();
	
	/**
	 * Entity Item Rotation.
	 */
	public Vec3 entityItemRotation = initVec();
	
	/**
	 * Handle render Inventory or not
	 */
	public boolean renderInventory = true;
	
	/**
	 * Handle render EntityItem or not
	 */
	public boolean renderEntityItem = true;
	
	/**
	 * Spin item all the time insides the inventory?
	 */
	public boolean inventorySpin = true;
	
	public RenderModelItem(IItemModel mdl, ResourceLocation texture) {
		model = mdl;
		texturePath = texture;
	}
	
	public RenderModelItem setRenderInventory(boolean b) {
		renderInventory = b;
		return this;
	}
	
	public RenderModelItem setRenderEntityItem(boolean b) {
		renderEntityItem = b;
		return this;
	}
	
	public RenderModelItem setInventorySpin(boolean b) {
		inventorySpin = b;
		return this;
	}
	
	public RenderModelItem setStdRotation(float x, float y, float z) {
		initVec(stdRotation, x, y, z);
		return this;
	}
	
	public RenderModelItem setEquipRotation(float x, float y, float z) {
		initVec(equipRotation, x, y, z);
		return this;
	}
	
	public RenderModelItem setInvRotation(float x, float y, float z) {
		initVec(invRotation, x, y, z);
		return this;
	}
	
	public RenderModelItem setEntityItemRotation(float b0, float b1, float b2) {
		initVec(entityItemRotation, b0, b1, b2);
		return this;
	}
	
	public RenderModelItem setScale(double s) {
		scale = s;
		return this;
	}
	
	public RenderModelItem setInvScale(double s) {
		invScale = s;
		return this;
	}
	
	public RenderModelItem setOffset(float offsetX, float offsetY, float offsetZ) {
		initVec(stdOffset, offsetX, offsetY, offsetZ);
		return this;
	}
	
	public RenderModelItem setInvOffset(float offsetX, float offsetY) {
		this.invOffset.set(offsetX, offsetY);
		return this;
	}
	
	public RenderModelItem setEquipOffset(double b0, double b1, double b2) {
		initVec(equipOffset, b0, b1, b2);
		return this;
	}
	
	public RenderModelItem setInformationFrom(RenderModelItem a) {
		
		this.renderInventory = a.renderInventory;
		this.invOffset = a.invOffset;
		this.setInvScale(a.invScale);
		
		this.stdOffset = a.stdOffset;
		this.stdRotation = a.stdRotation;
		
		this.scale = a.scale;
		return this;
	}
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
			return true;
		case ENTITY:
			return renderEntityItem;
		case INVENTORY:
			return renderInventory;

		default:
			return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		switch (helper) {
		case ENTITY_ROTATION:
		case ENTITY_BOBBING:
			return true;

		default:
			return false;

		}
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch (type) {
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
			renderEquipped(item, (RenderBlocks) data[0], (EntityLivingBase) data[1], type);
			break;
		case ENTITY:
			renderEntityItem((RenderBlocks)data[0], (EntityItem) data[1]);
			break;
		case INVENTORY:
			renderInventory();
			break;
		default:
			break;

		}
	}
	
	public void renderInventory() {
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glPushMatrix(); {
			
			RenderUtils.loadTexture(texturePath);
			
			GL11.glTranslatef(8.0F + invOffset.x, 8.0F + invOffset.y, 0.0F);
			GL11.glScaled(16F * invScale, 16F * invScale, 16F * invScale);
			float rotation = 145F;
			if(inventorySpin) rotation = Minecraft.getSystemTime() / 100F;
			GL11.glRotatef(rotation, 0, 1, 0);
			this.doRotation(invRotation);
			GL11.glScalef(-1F, -1F, 1F);
			
			renderAtStdPosition();
			
		} GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	public void renderEntityItem(RenderBlocks render, EntityItem ent) {
		
		GL11.glPushMatrix(); {
			RenderUtils.loadTexture(texturePath);
			this.doRotation(entityItemRotation);
			renderAtStdPosition();
		} GL11.glPopMatrix();
	}
	
	public void renderEquipped(ItemStack item, RenderBlocks render,
			EntityLivingBase entity, ItemRenderType type) {

		if (item.stackTagCompound == null)
			item.stackTagCompound = new NBTTagCompound();
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix(); {
			RenderUtils.loadTexture(texturePath);
			float sc2 = 0.0625F;
			GL11.glRotatef(40F, 0, 0, 1);
			this.doTransformation(equipOffset);
			this.doRotation(equipRotation);
			GL11.glRotatef(90, 0, -1, 0);
			renderAtStdPosition(getModelAttribute(item, entity));
		} GL11.glPopMatrix();
		
	}

	protected final void renderAtStdPosition() {
		renderAtStdPosition(0.0F);
	}
	
	protected final void renderAtStdPosition(float i) {
		GL11.glScaled(scale, scale, scale);
		GL11.glDisable(GL11.GL_CULL_FACE);
		this.doTransformation(stdOffset);
		GL11.glScalef(-1F , -1F , 1F );
		GL11.glRotated(stdRotation.yCoord + 180, 0.0F, 1.0F, 0.0F); //历史遗留问题，没什么必要改啦=w=
		GL11.glRotated(stdRotation.zCoord, 0.0F, 0.0F, 1.0F);
		GL11.glRotated(stdRotation.xCoord, 1.0F, 0.0F, 0.0F);
		
		model.render(null, 0.0625F, i);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	protected float getModelAttribute(ItemStack item, EntityLivingBase entity) {
		return 0.1F;
	}
	
	protected void doTransformation(Vec3 vec3) {
		if(vec3 != null)
			GL11.glTranslated(vec3.xCoord, vec3.yCoord, vec3.zCoord);
	}
	
	protected void doRotation(Vec3 vec3) {
		if(vec3 != null) {
			GL11.glRotated(vec3.yCoord, 0.0F, 1.0F, 0.0F);
			GL11.glRotated(vec3.zCoord, 0.0F, 0.0F, 1.0F);
			GL11.glRotated(vec3.xCoord, 1.0F, 0.0F, 0.0F);
		}
	}
	
	private static void initVec(Vec3 vec) {
		vec = vec == null ?  Vec3.createVectorHelper(0D, 0D, 0D) : vec;
	}
	
	private static Vec3 initVec() {
		return Vec3.createVectorHelper(0D, 0D, 0D);
	}
	
	private static void initVec(Vec3 vec, double x, double y, double z) {
		if(vec == null)
			vec = Vec3.createVectorHelper(x, y, z);
		else {
			vec.xCoord = x;
			vec.yCoord = y;
			vec.zCoord = z;
		}
	}

}
