package com.getitemfromblock.create_tweaked_controllers.input;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.GenericInputScreen;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.MouseButtonScreen;
import com.simibubi.create.AllKeys;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Mouse button input implementation for the controller mapping system.
 * <p>
 * Reads mouse button states using Create's AllKeys utility. Supports invert option.
 * When used as an axis, returns 1.0 when pressed and 0.0 when released.
 */
public class MouseButtonInput implements GenericInput
{
    /** Mouse button index (0 = left, 1 = right, 2 = middle, etc.). -1 means unbound. */
    public int buttonID = -1;
    /** If true, the button state is inverted. */
    public boolean invertValue = false;

    public MouseButtonInput(int button)
    {
        this.buttonID = button;
    }
    
    public MouseButtonInput()
    {
    }

    @Override
    public boolean GetButtonValue()
    {
        if (!IsInputValid()) return invertValue;
        return invertValue ? !AllKeys.isMouseButtonDown(buttonID) : AllKeys.isMouseButtonDown(buttonID);
    }

    @Override
    public float GetAxisValue()
    {
        return GetButtonValue() ? 1.0f : 0.0f;
    }

    @Override
    public MutableComponent GetDisplayName()
    {
        return CreateTweakedControllers.translateDirect("gui_input_mouse", ""+buttonID);
    }

    @Override
    public boolean IsInputValid()
    {
        return buttonID >= 0;
    }

    @Override
    public void Serialize(DataOutputStream buf) throws IOException
    {
        buf.writeBoolean(invertValue);
        buf.writeInt(buttonID);
    }

    @Override
    public void Deserialize(DataInputStream buf) throws IOException
    {
        invertValue = buf.readBoolean();
        buttonID = buf.readInt();
    }

    @Override
    public InputType GetType()
    {
        return InputType.MOUSE_BUTTON;
    }

    @Override
    public int GetValue()
    {
        return buttonID;
    }

    @Override
    public GenericInputScreen OpenConfigScreen(Screen previous, Component comp)
    {
        return new MouseButtonScreen(previous, comp, this);
    }
    
}
