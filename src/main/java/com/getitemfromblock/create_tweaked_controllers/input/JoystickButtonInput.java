package com.getitemfromblock.create_tweaked_controllers.input;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.GenericInputScreen;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.JoystickButtonScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class JoystickButtonInput implements GenericInput
{
    public int buttonID = -1;
    public boolean invertValue = false;
    public int deviceIndex = 0;
    // Transient: used during deserialization to decide whether the stream carries a device
    // index (new profiles do; legacy ones don't). Never written to disk directly.
    public transient boolean hasDeviceIndex = true;

    public JoystickButtonInput(int buttonID)
    {
        this.buttonID = buttonID;
    }

    public JoystickButtonInput(int deviceIndex, int buttonID)
    {
        this.deviceIndex = deviceIndex;
        this.buttonID = buttonID;
    }

    public JoystickButtonInput()
    {
    }

    @Override
    public boolean GetButtonValue()
    {
        if (!IsInputValid()) return invertValue;
        return invertValue ? !JoystickInputs.GetButton(deviceIndex, buttonID) : JoystickInputs.GetButton(deviceIndex, buttonID);
    }

    @Override
    public float GetAxisValue()
    {
        return GetButtonValue() ? 1.0f : 0.0f;
    }

    @Override
    public MutableComponent GetDisplayName()
    {
        return CreateTweakedControllers.translateDirect("gui_input_joystick_button", ""+deviceIndex+":"+buttonID);
    }

    @Override
    public boolean IsInputValid()
    {
        return buttonID < JoystickInputs.GetButtonCount(deviceIndex) && buttonID >= 0;
    }

    @Override
    public void Serialize(DataOutputStream buf) throws IOException
    {
        buf.writeBoolean(invertValue);
        buf.writeInt(buttonID);
        buf.writeInt(deviceIndex);
    }

    @Override
    public void Deserialize(DataInputStream buf) throws IOException
    {
        invertValue = buf.readBoolean();
        buttonID = buf.readInt();
        if (hasDeviceIndex)
        {
            deviceIndex = buf.readInt();
        }
        else
        {
            deviceIndex = 0;
        }
    }

    @Override
    public InputType GetType()
    {
        return InputType.JOYSTICK_BUTTON;
    }

    @Override
    public int GetValue()
    {
        return buttonID;
    }

    @Override
    public GenericInputScreen OpenConfigScreen(Screen previous, Component comp)
    {
        return new JoystickButtonScreen(previous, comp, this);
    }
    
}
