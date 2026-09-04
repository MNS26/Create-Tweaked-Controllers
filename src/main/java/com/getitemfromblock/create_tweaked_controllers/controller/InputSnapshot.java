package com.getitemfromblock.create_tweaked_controllers.controller;

/**
 * Immutable snapshot of a player's controller input state at a point in time.
 * <p>
 * Used to transmit and cache another client's input for rendering on the local client.
 * Button states are packed into a {@code short} (15 bits, one per button) and axis
 * states are packed into an {@code int} using the same encoding as
 * {@link ControllerRedstoneOutput}.
 *
 * @see TweakedLinkedControllerServerHandler
 * @see TweakedLinkedControllerClientHandler
 */
public record InputSnapshot(short buttons, int axis, long timestamp)
{
    public static final InputSnapshot EMPTY = new InputSnapshot((short) 0, 0, 0);

    /**
     * Returns whether the given button index is pressed.
     *
     * @param index button index (0-14)
     * @return true if the button is pressed
     */
    public boolean getButton(int index)
    {
        return (buttons & (1 << index)) != 0;
    }

    /**
     * Decodes the given axis index as a float in the range [-1.0, 1.0].
     * <p>
     * Uses the same bit layout as {@link ControllerRedstoneOutput#DecodeAxis(int)}:
     * <ul>
     *   <li>Joystick axes (0-3): 5 bits each (1 sign + 4 value bits), range -15 to +15</li>
     *   <li>Trigger axes (4-5): 4 bits each, range 0 to 15</li>
     * </ul>
     *
     * @param index axis index (0-5)
     * @return axis value as a float in [-1.0, 1.0]
     */
    public float getAxisValue(int index)
    {
        if (index < 0 || index > 5)
            return 0;

        byte raw;
        if (index < 4)
        {
            raw = (byte) ((axis & (0x1f << index * 5)) >>> index * 5);
        }
        else
        {
            int shift = index == 4 ? 20 : 24;
            raw = (byte) ((axis & (0xf << shift)) >>> shift);
        }

        if (index < 4)
        {
            // Joystick axis: sign bit + 4 value bits
            boolean negative = (raw & 0x10) != 0;
            float value = (raw & 0x0f) / 15.0f;
            return negative ? -value : value;
        }
        else
        {
            // Trigger axis: always positive, 0-1 mapped to -1..+1
            return (raw / 15.0f) * 2 - 1;
        }
    }
}
