package com.getitemfromblock.create_tweaked_controllers.input;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.getitemfromblock.create_tweaked_controllers.gui.InputConfig.GenericInputScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Polymorphic interface for all input device types in the controller system.
 * <p>
 * Each implementation represents a specific physical input (joystick button, joystick axis,
 * keyboard key, mouse button, mouse axis, or mouse wheel). Inputs can operate in two modes:
 * <ul>
 *   <li><b>Button mode</b> ({@link #GetButtonValue()}) - binary on/off, used for the 15 controller buttons</li>
 *   <li><b>Axis mode</b> ({@link #GetAxisValue()}) - continuous 0.0-1.0 value, used for the 6 controller axes</li>
 * </ul>
 * Inputs are serialized/deserialized to binary profile files for persistence.
 *
 * @see JoystickButtonInput
 * @see JoystickAxisInput
 * @see KeyboardInput
 * @see MouseButtonInput
 * @see MouseAxisInput
 * @see MouseWheelInput
 */
public interface GenericInput
{
    /** Returns true if this input is currently pressed/active. */
    boolean GetButtonValue();

    /** Returns a continuous value between 0.0 and 1.0 representing the axis position. */
    float GetAxisValue();

    /** Returns a human-readable display name for this input (e.g. "Gamepad 0:3"). */
    MutableComponent GetDisplayName();

    /** Returns true if this input is properly configured and the device is available. */
    boolean IsInputValid();

    /** Serializes this input's configuration to a binary stream for profile persistence. */
    void Serialize(DataOutputStream buf) throws IOException;

    /** Deserializes this input's configuration from a binary stream. */
    void Deserialize(DataInputStream buf) throws IOException;

    /** Opens the configuration GUI screen for editing this input's parameters. */
    GenericInputScreen OpenConfigScreen(Screen previous, Component name);

    /** Returns the type discriminator for this input (used in serialization and display). */
    InputType GetType();

    /** Returns the raw device-specific value (button ID, axis ID, key code, etc.). */
    int GetValue();

    /**
     * Enum identifying the type of each input for serialization and display purposes.
     * Ordinal values are used as byte identifiers in profile files.
     */
    enum InputType
    {
        NONE,
        JOYSTICK_BUTTON,
        JOYSTICK_AXIS,
        MOUSE_BUTTON,
        MOUSE_AXIS,
        MOUSE_WHEEL,
        KEYBOARD_KEY;

        public static InputType GetType(byte v)
        {
            return values()[v];
        }

        public static byte GetValue(InputType v)
        {
            return (byte)v.ordinal();
        }
    }
}
