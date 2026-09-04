package com.getitemfromblock.create_tweaked_controllers.controller;

import com.getitemfromblock.create_tweaked_controllers.input.JoystickInputs;

/**
 * Enumeration of available controller profile types.
 * <p>
 * Each type corresponds to a different input mapping configuration:
 * <ul>
 *   <li>{@link #KEYBOARD_MOUSE} - default keyboard+mouse layout</li>
 *   <li>{@link #JOYSTICK} - default gamepad layout using GLFW gamepad API</li>
 *   <li>{@link #CUSTOM_0} / {@link #CUSTOM_1} - user-defined custom mappings</li>
 * </ul>
 * The "adapted" state indicates whether the current control type matches the
 * available hardware (e.g. JOYSTICK is adapted when a joystick is connected).
 *
 * @see ControlProfile
 * @see TweakedControlsUtil
 */
public enum ControlType
{
    KEYBOARD_MOUSE,
    JOYSTICK,
    CUSTOM_0,
    CUSTOM_1;

    public boolean IsAdapted()
    {
        switch (this)
        {
            case KEYBOARD_MOUSE:
                return !JoystickInputs.HasJoystick();
            case JOYSTICK:
                return JoystickInputs.HasJoystick();
            default:
                return true;
        }
    }
}