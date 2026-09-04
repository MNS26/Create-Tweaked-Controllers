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

/**
 * Joystick axis input implementation for the controller mapping system.
 * <p>
 * Reads analog axis values from a GLFW joystick device. Supports:
 * <ul>
 *   <li>Per-device addressing via {@link #deviceIndex} (supports up to 16 devices)</li>
 *   <li>Configurable min/max bounds for normalizing the raw axis range</li>
 *   <li>A deadzone threshold to filter out noise from resting stick positions</li>
 * </ul>
 * The raw axis value (typically -1.0 to 1.0) is mapped through the bounds to produce
 * a 0.0-1.0 output, with values below the deadzone treated as zero.
 *
 * @see JoystickInputs
 * @see JoystickAxisScreen
 */
public class JoystickAxisInput implements GenericInput
{
    /** The axis index within the joystick device (e.g. 0-5 for typical gamepads). */
    public int axisID = -1;
    /** Minimum bound for axis normalization. Values at or below this map to 0.0. */
    public float minBound = 0.0f;
    /** Maximum bound for axis normalization. Values at or above this map to 1.0. */
    public float maxBound = 1.0f;
    /** Deadzone threshold: raw axis values with absolute value below this are treated as zero. */
    public float deadzone = 0.0f;
    /** Index of the joystick device (0-15). Multiple controllers can be addressed independently. */
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
