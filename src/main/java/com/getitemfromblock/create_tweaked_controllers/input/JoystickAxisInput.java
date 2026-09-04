package com.getitemfromblock.create_tweaked_controllers.input;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.getitemfromblock.create_tweaked_controllers.CreateTweakedControllers;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.GenericInputScreen;
import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.JoystickAxisScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class JoystickAxisInput implements GenericInput
{
    public int axisID = -1;
    public float minBound = 0.0f;
    public float maxBound = 1.0f;
    public float deadzone = 0.0f;
    public int deviceIndex = 0;
    // Transient: used during deserialization to decide whether the stream carries a device
    // index (new profiles do; legacy ones don't). Never written to disk directly.
    public transient boolean hasDeviceIndex = true;
    // Transient: used during deserialization to decide whether the stream carries a device
    // deadzones (new profiles do; legacy ones don't). Never written to disk directly.
    public transient boolean hasDeviceDeadzone = true;

    public JoystickAxisInput(int axisID)
    {
        this.axisID = axisID;
    }

    public JoystickAxisInput()
    {
    }

    public JoystickAxisInput(int axisID, float min, float max)
    {
        this.axisID = axisID;
        this.minBound = min;
        this.maxBound = max;
    }

    public JoystickAxisInput(int deviceIndex, int axisID, float min, float max)
    {
        this.deviceIndex = deviceIndex;
        this.axisID = axisID;
        this.minBound = min;
        this.maxBound = max;
    }

    @Override
    public boolean GetButtonValue()
    {
        return GetAxisValue() >= 0.5f;
    }

    @Override
    public float GetAxisValue()
    {
        if (!IsInputValid()) return 0;
        float raw = JoystickInputs.GetAxis(deviceIndex, axisID);
        if (Math.abs(raw) < deadzone) return 0;
        float v = (raw - minBound) / (maxBound - minBound);
        if (v < 0) v = 0;
        if (v > 1) v = 1;
        return v;
    }

    @Override
    public MutableComponent GetDisplayName()
    {
        if (minBound >= 0 && maxBound >= 0)
        {
            return CreateTweakedControllers.translateDirect("gui_input_joystick_axis", "+"+deviceIndex+":"+axisID);
        }
        else if (minBound <= 0 && maxBound <= 0)
        {
            return CreateTweakedControllers.translateDirect("gui_input_joystick_axis", "-"+deviceIndex+":"+axisID);
        }
        else
        {
            return CreateTweakedControllers.translateDirect("gui_input_joystick_axis", ""+deviceIndex+":"+axisID);
        }
    }

    @Override
    public boolean IsInputValid()
    {
        return axisID < JoystickInputs.GetAxisCount(deviceIndex) && axisID >= 0 && minBound != maxBound;
    }

    @Override
    public void Serialize(DataOutputStream buf) throws IOException
    {
        buf.writeFloat(minBound);
        buf.writeFloat(maxBound);
        buf.writeInt(axisID);
        buf.writeInt(deviceIndex);
        buf.writeFloat(deadzone);
    }

    @Override
    public void Deserialize(DataInputStream buf) throws IOException
    {
        minBound = buf.readFloat();
        maxBound = buf.readFloat();
        axisID = buf.readInt();
        if (hasDeviceIndex)
        {
            deviceIndex = buf.readInt();
        }
        else
        {
            deviceIndex = 0;
        }
        if (hasDeviceDeadzone)
        {
            deadzone = buf.readFloat();
        }
        else
        {
            deadzone = 0.0f;
        }
    }

    @Override
    public InputType GetType()
    {
        return InputType.JOYSTICK_AXIS;
    }

    @Override
    public int GetValue()
    {
        return axisID;
    }

    @Override
    public GenericInputScreen OpenConfigScreen(Screen previous, Component comp)
    {
        return new JoystickAxisScreen(previous, comp, this);
    }

    public float GetRawInput()
    {
        if (!IsInputValid()) return 0;
        return JoystickInputs.GetAxis(deviceIndex, axisID);
    }
    
}
