package com.getitemfromblock.create_tweaked_controllers.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;
import com.getitemfromblock.create_tweaked_controllers.config.ModClientConfig;
import com.getitemfromblock.create_tweaked_controllers.controller.InputSnapshot;
import com.getitemfromblock.create_tweaked_controllers.controller.TweakedLinkedControllerClientHandler;
import com.getitemfromblock.create_tweaked_controllers.controller.TweakedLinkedControllerClientHandler.Mode;
import com.getitemfromblock.create_tweaked_controllers.input.GamepadInputs;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Custom item renderer for the Tweaked Linked Controller.
 * <p>
 * Renders the controller as a 3D model with animated buttons, joysticks, and triggers.
 * Supports three controller layout styles (Xbox, Nintendo, PlayStation) via partial models.
 * <p>
 * Animation is driven by {@link LerpedFloat} interpolation:
 * <ul>
 *   <li>Button presses are smoothly animated with exponential chasers</li>
 *   <li>Joystick tilt is linearly interpolated to match the physical stick position</li>
 *   <li>Trigger pull is linearly interpolated based on axis value</li>
 * </ul>
 * The renderer handles both first-person hand rendering and lectern block rendering,
 * with equip progress animation when switching between active/inactive states.
 *
 * @see TweakedLecternControllerRenderer
 * @see TweakedLinkedControllerClientHandler
 */
public class TweakedLinkedControllerItemRenderer extends CustomRenderedItemModelRenderer
{
    protected static final PartialModel BASE = PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/powered"));
    protected static final PartialModel[] CONTROLLERS =
    {
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/controller_x")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/controller_n")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/controller_p"))

    };
    protected static final PartialModel BUTTON = PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button"));
    protected static final PartialModel JOYSTICK = PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/joystick"));
    protected static final PartialModel TRIGGER = PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/trigger"));
    protected static final PartialModel[] BUTTONS_LEFT =
    {
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_x_x")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_n_y")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_p_s"))
    };
    protected static final PartialModel[] BUTTONS_UP =
    {
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_x_y")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_n_x")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_p_t"))
    };
    protected static final PartialModel[] BUTTONS_DOWN =
    {
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_x_a")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_n_b")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_p_x"))
    };
    protected static final PartialModel[] BUTTONS_RIGHT =
    {
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_x_b")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_n_a")),
            PartialModel.of(CreateTweakedControllers.asResource("item/tweaked_linked_controller/button_p_c"))
    };
    static LerpedFloat equipProgress;
    static ArrayList<LerpedFloat> buttons;
    static ArrayList<LerpedFloat> axis;
    /** UUID of the player whose inputs should drive the lectern render, or null for local rendering. */
    static UUID renderTargetPlayerUUID = null;
    /** Per-player smoothed animation state for remote lectern rendering. */
    static final Map<UUID, RemoteAnimState> remoteAnimStates = new HashMap<>();
    /** Set while rendering a remote player's lectern so the draw helpers select remote animation. */
    private static boolean renderRemote = false;

    static
    {
        equipProgress = LerpedFloat.linear()
            .startWithValue(0);
        buttons = new ArrayList<>(15);
        for (int i = 0; i < 15; i++)
            buttons.add(LerpedFloat.linear()
                .startWithValue(0));
        axis = new ArrayList<>(6);
        for (int i = 0; i < 6; i++)
            axis.add(LerpedFloat.linear()
                .startWithValue(i < 4 ? 0 : -1));
    }

    static class RemoteAnimState
    {
        final ArrayList<LerpedFloat> buttons = new ArrayList<>(15);
        final ArrayList<LerpedFloat> axis = new ArrayList<>(6);

        RemoteAnimState()
        {
            for (int i = 0; i < 15; i++)
                buttons.add(LerpedFloat.linear()
                    .startWithValue(0));
            for (int i = 0; i < 6; i++)
                axis.add(LerpedFloat.linear()
                    .startWithValue(i < 4 ? 0 : -1));
        }
    }

    public static void setRenderTarget(UUID playerUUID)
    {
        renderTargetPlayerUUID = playerUUID;
    }

    public static void updateRemoteAnimation()
    {
        if (Minecraft.getInstance().isPaused())
            return;
        for (Map.Entry<UUID, InputSnapshot> entry : TweakedLinkedControllerClientHandler.remoteInputs.entrySet())
        {
            RemoteAnimState state = remoteAnimStates.computeIfAbsent(entry.getKey(), $ -> new RemoteAnimState());
            InputSnapshot snapshot = entry.getValue();
            if (snapshot == null)
                continue;
            for (int i = 0; i < state.buttons.size(); i++)
            {
                LerpedFloat lerpedFloat = state.buttons.get(i);
                lerpedFloat.chase(snapshot.getButton(i) ? 1 : 0, .4f, Chaser.EXP);
                lerpedFloat.tickChaser();
            }
            for (int i = 0; i < state.axis.size(); i++)
            {
                LerpedFloat lerpedFloat = state.axis.get(i);
                lerpedFloat.chase(snapshot.getAxisValue(i), 1.0f, Chaser.LINEAR);
                lerpedFloat.tickChaser();
            }
        }
        remoteAnimStates.keySet()
            .removeIf(uuid -> !TweakedLinkedControllerClientHandler.remoteInputs.containsKey(uuid));
    }

    public static void earlyTick()
    {
        if (Minecraft.getInstance().isPaused())
            return;

        updateRemoteAnimation();

        boolean active = TweakedLinkedControllerClientHandler.MODE != Mode.IDLE;
        equipProgress.chase(active ? 1 : 0, .2f, Chaser.EXP);
        equipProgress.tickChaser();
    }

    public static void tick()
    {
        if (Minecraft.getInstance().isPaused() || TweakedLinkedControllerClientHandler.MODE == Mode.IDLE)
            return;

        for (int i = 0; i < buttons.size(); i++)
        {
            LerpedFloat lerpedFloat = buttons.get(i);
            lerpedFloat.chase(GamepadInputs.buttons[i] ? 1 : 0, .4f, Chaser.EXP);
            lerpedFloat.tickChaser();
        }
        for (int i = 0; i < axis.size(); i++)
        {
            LerpedFloat lerpedFloat = axis.get(i);
            lerpedFloat.chase(GamepadInputs.axis[i], 1.0f, Chaser.LINEAR);
            lerpedFloat.tickChaser();

        }
    }

    public static void resetButtons()
    {
        for (int i = 0; i < buttons.size(); i++)
        {
            buttons.get(i).startWithValue(0);
        }
        for (int i = 0; i < axis.size(); i++)
        {
            axis.get(i).startWithValue(i < 4 ? 0.0f : -1.0f);
        }
    }

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
        ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light,
        int overlay)
    {
        renderNormal(stack, model, renderer, transformType, ms, light);
    }

    protected static void renderNormal(ItemStack stack, CustomRenderedItemModel model,
          PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
          int light)
    {
        render(stack, model, renderer, transformType, ms, light, RenderType.NORMAL, false, false);
    }

    public static void renderInLectern(ItemStack stack, CustomRenderedItemModel model,
          PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
          int light, boolean active, boolean renderDepression)
    {
        render(stack, model, renderer, transformType, ms, light, RenderType.LECTERN, active, renderDepression);
    }

    private static final Vec3[] positionList =
    {
        new Vec3(3, 0.9, 11.5).multiply(1/16.0, 1/16.0, 1/16.0), // SHOULDER BUTTONS
        new Vec3(3, 0.9, 2.5).multiply(1/16.0, 1/16.0, 1/16.0),

        new Vec3(6, 1, 8.5).multiply(1/16.0, 1/16.0, 1/16.0), // FACE BUTTONS
        new Vec3(6, 1, 6.5).multiply(1/16.0, 1/16.0, 1/16.0),
        new Vec3(5, 1, 7.5).multiply(1/16.0, 1/16.0, 1/16.0),

        new Vec3(6, 0.5, 11.5).multiply(1/16.0, 1/16.0, 1/16.0), // JOYSTICK
        new Vec3(9, 0.5, 5.5).multiply(1/16.0, 1/16.0, 1/16.0),

        new Vec3(8, 1, 9.5).multiply(1/16.0, 1/16.0, 1/16.0), // DPAD
        new Vec3(9, 1, 8.5).multiply(1/16.0, 1/16.0, 1/16.0),
        new Vec3(10, 1, 9.5).multiply(1/16.0, 1/16.0, 1/16.0),
        new Vec3(9, 1, 10.5).multiply(1/16.0, 1/16.0, 1/16.0),

        new Vec3(3, -0.1, 11.5).multiply(1/16.0, 1/16.0, 1/16.0), // TRIGGERS
        new Vec3(3, -0.1, 2.5).multiply(1/16.0, 1/16.0, 1/16.0),
    };

    protected static void render(ItemStack stack, CustomRenderedItemModel model,
          PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms,
          int light, RenderType renderType, boolean active, boolean renderDepression)
    {
        float pt = AnimationTickHolder.getPartialTicks();
        var msr = TransformStack.of(ms);

        ms.pushPose();

        Minecraft mc = Minecraft.getInstance();
        renderRemote = renderType == RenderType.LECTERN && renderTargetPlayerUUID != null
            && (mc.player == null || !renderTargetPlayerUUID.equals(mc.player.getUUID()));
        if (renderType == RenderType.NORMAL && mc.player != null)
        {
            boolean rightHanded = mc.options.mainHand().get() == HumanoidArm.RIGHT;
            ItemDisplayContext mainHand =
                    rightHanded ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            ItemDisplayContext offHand =
                    rightHanded ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

            active = false;
            boolean noControllerInMain = !ModItems.TWEAKED_LINKED_CONTROLLER.isIn(mc.player.getMainHandItem());

            if (transformType == mainHand || (transformType == offHand && noControllerInMain))
            {
                float equip = equipProgress.getValue(pt);
                int handModifier = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
                if ((transformType == mainHand && mc.player.getOffhandItem().isEmpty())
                    || (transformType == offHand && mc.player.getMainHandItem().isEmpty()))
                    {
                        // Number calculated based on the displacement in the model file and its scaling
                        final float modelDisplacementValue = 0.93106617f;
                        msr.translate(0.1f * equip, equip / 3, equip * handModifier * modelDisplacementValue);
                        msr.rotateZDegrees(equip * -10);
                    }
                    else
                    {
                        msr.translate(0, equip / 4, equip / 4 * handModifier);
                        msr.rotateYDegrees(equip * -30 * handModifier);
                        msr.rotateZDegrees(equip * -30);
                    }
                active = true;
            }

            if (transformType == ItemDisplayContext.GUI)
            {
                if (stack == mc.player.getMainHandItem())
                    active = true;
                if (stack == mc.player.getOffhandItem() && noControllerInMain)
                    active = true;
            }

            active &= TweakedLinkedControllerClientHandler.MODE != Mode.IDLE;

            renderDepression = true;
        }

        int c = ModClientConfig.CONTROLLER_LAYOUT_TYPE.get().ordinal();
        if (active)
        {
            renderer.render(BASE.get(), light);
        }
        else
        {
            renderer.render(CONTROLLERS[c].get(), light);
            ms.popPose();
            return;
        }

        float s = 1 / 16f;
        float b = s * -.75f;
        int index = 0;
        if (renderType == RenderType.NORMAL && TweakedLinkedControllerClientHandler.MODE == Mode.BIND)
        {
            int i = (int) Mth.lerp((Mth.sin(AnimationTickHolder.getRenderTime() / 4f) + 1) / 2, 5, 15);
            light = i << 20;
        }

        ms.pushPose();
        BakedModel button = BUTTONS_DOWN[c].get();
        renderButton(renderer, ms, light, pt, button, b, index++, renderDepression, false);
        button = BUTTONS_RIGHT[c].get();
        renderButton(renderer, ms, light, pt, button, b, index++, renderDepression, false);
        button = BUTTONS_LEFT[c].get();
        renderButton(renderer, ms, light, pt, button, b, index++, renderDepression, false);
        button = BUTTONS_UP[c].get();
        renderButton(renderer, ms, light, pt, button, b, index++, renderDepression, false);
        button = TRIGGER.get();
        for (; index < 6; index++)
        {
            ms.pushPose();
            msr.translate(positionList[index - 4]);
            renderButton(renderer, ms, light, pt, button, b, index, renderDepression, true);
            ms.popPose();
        }
        button = BUTTON.get();
        for (; index < 15; index++)
        {
            if (index == 9 || index == 10) continue;
            ms.pushPose();
            msr.translate(positionList[index - 4]);
            renderButton(renderer, ms, light, pt, button, b, index, renderDepression, false);
            ms.popPose();
        }
        button = JOYSTICK.get();
        renderJoystick(renderer, ms, light, pt, button, b, renderDepression, false);
        renderJoystick(renderer, ms, light, pt, button, b, renderDepression, true);
        button = TRIGGER.get();
        renderTrigger(renderer, ms, light, pt, button, false);
        renderTrigger(renderer, ms, light, pt, button, true);
        
        ms.popPose();
        ms.popPose();
    }

    protected static LerpedFloat buttonAnim(int index)
    {
        if (renderRemote && renderTargetPlayerUUID != null)
        {
            RemoteAnimState state = remoteAnimStates.get(renderTargetPlayerUUID);
            if (state != null)
                return state.buttons.get(index);
        }
        return buttons.get(index);
    }

    protected static LerpedFloat axisAnim(int index)
    {
        if (renderRemote && renderTargetPlayerUUID != null)
        {
            RemoteAnimState state = remoteAnimStates.get(renderTargetPlayerUUID);
            if (state != null)
                return state.axis.get(index);
        }
        return axis.get(index);
    }

    protected static void renderButton(PartialItemModelRenderer renderer, PoseStack ms, int light, float pt, BakedModel button,
        float b, int index, boolean renderDepression, boolean isSideway)
        {
            ms.pushPose();
            if (renderDepression)
            {
                float depression = b * buttonAnim(index).getValue(pt);
                if (isSideway)
                {
                    ms.translate(-depression, 0, 0);
                }
                else
                {
                    ms.translate(0, depression, 0);
                }
            }
            renderer.renderSolid(button, light);
            ms.popPose();
    }

    protected static void renderTrigger(PartialItemModelRenderer renderer, PoseStack ms, int light, float pt, BakedModel trigger, boolean isRight)
        {
            ms.pushPose();
            final float delta = 1 / 16f * -0.75f;
            Vec3 pos = positionList[isRight ? 12 : 11];
            float value = axisAnim(isRight ? 5 : 4).getValue(pt);
            value = (value + 1) / 2 * delta;
            ms.translate(pos.x - value, pos.y, pos.z);
            renderer.renderSolid(trigger, light);
            ms.popPose();
    }

    protected static void renderJoystick(PartialItemModelRenderer renderer, PoseStack ms, int light, float pt, BakedModel joystick,
        float b, boolean renderDepression, boolean isRight)
        {
            ms.pushPose();
            final double delta = 7.5/16;
            Vec3 pos = positionList[isRight ? 6 : 5].subtract(delta, delta, delta);
            ms.translate(pos.x, pos.y, pos.z);
            ms.pushPose();
            float x, y;
            if (isRight)
            {
                x = axisAnim(2).getValue(pt);
                y = axisAnim(3).getValue(pt);
            }
            else
            {
                x = axisAnim(0).getValue(pt);
                y = axisAnim(1).getValue(pt);
            }
            Vector3f ax = new Vector3f(-x, 0, -y);
            double angle = x * x + y * y;
            angle = Math.min(Math.sqrt(angle), 1.0) * 0.6f;
            if (ax.dot(ax) < 0.001f)
            {
                ax = new Vector3f(-1,0,-1);
                angle = 0;
            }
            ax.normalize();
            ms.mulPose(new Quaternionf(new AxisAngle4f((float)angle, ax)));
            if (renderDepression)
            {
                float depression = b * buttonAnim(isRight ? 10 : 9).getValue(pt);
                ms.translate(0, depression, 0);
            }
            renderer.renderSolid(joystick, light);
            ms.popPose();
            ms.popPose();
    }

    protected enum RenderType
    {
        NORMAL,
        LECTERN
    }

}
