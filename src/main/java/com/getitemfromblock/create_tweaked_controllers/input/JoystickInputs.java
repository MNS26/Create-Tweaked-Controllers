package com.getitemfromblock.create_tweaked_controllers.input;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

/**
 * Low-level GLFW joystick polling system that reads raw button and axis states.
 * <p>
 * Supports up to 16 joystick devices simultaneously. Each device's state is tracked
 * independently, including both current and "stored" values (used by the binding UI
 * to detect changes). The system auto-selects a primary device for backwards-compatible
 * single-device API calls.
 * <p>
 * Key operations:
 * <ul>
 *   <li>{@link #GetControls()} - polls all connected joysticks and updates state</li>
 *   <li>{@link #GetButton(int, int)} / {@link #GetAxis(int, int)} - read device state</li>
 *   <li>{@link #StoreAxisValues(int)} / {@link #GetFirstAxis(int)} - change detection for binding UI</li>
 * </ul>
 *
 * @see GamepadInputs
 * @see JoystickAxisInput
 * @see JoystickButtonInput
 */
public class JoystickInputs
{
    /** Internal state container for a single joystick device. */
    private static class DeviceState
    {
        Vector<Boolean> buttons = new Vector<>(0);
        Vector<Float> axis = new Vector<>(0);
        Vector<Boolean> storedButtons = new Vector<>(0);
        Vector<Float> storedAxis = new Vector<>(0);
    }

    /** Maximum number of joystick devices that can be tracked simultaneously. */
    private static final int MAX_JOYSTICKS = 16;
    private static final DeviceState[] devices = new DeviceState[MAX_JOYSTICKS];
    private static final List<Integer> presentDevices = new ArrayList<>();

    // Backwards compatibility: the index of the "primary" joystick, used by the simple
    // single-device API (GetButton(int), GetAxis(int), HasJoystick(), etc.)
    private static int selectedJoystick = -1;

    // Smaller threshold so precise HOTAS/sim axes (which rarely reach +/-0.75) still register.
    private static final float AXIS_CHANGE_THRESHOLD = 0.75f;

    public static void GetControls()
    {
        presentDevices.clear();
        int primary = -1;
        for (int i = 0; i < MAX_JOYSTICKS; i++)
        {
            if (!GLFW.glfwJoystickPresent(i))
            {
                devices[i] = null;
                continue;
            }
            if (primary == -1) primary = i;
            presentDevices.add(i);
            DeviceState state = devices[i];
            ByteBuffer b = GLFW.glfwGetJoystickButtons(i);
            FloatBuffer a = GLFW.glfwGetJoystickAxes(i);
            if (b == null && a == null)
            {
                devices[i] = null;
                continue;
            }
            if (state == null || state.buttons.size() != (b == null ? 0 : b.limit())
                || state.axis.size() != (a == null ? 0 : a.limit()))
            {
                state = new DeviceState();
                initDeviceState(state, b, a);
                devices[i] = state;
            }
            Fill(state, b, a);
        }
        if (presentDevices.isEmpty())
        {
            selectedJoystick = -1;
        }
        else
        {
            selectedJoystick = primary;
        }
    }

    private static void initDeviceState(DeviceState state, ByteBuffer b, FloatBuffer a)
    {
        int nButtons = b == null ? 0 : b.limit();
        int nAxis = a == null ? 0 : a.limit();
        state.buttons = new Vector<>(nButtons);
        state.storedButtons = new Vector<>(nButtons);
        for (int i = 0; i < nButtons; i++)
        {
            state.buttons.add(false);
            state.storedButtons.add(false);
        }
        state.axis = new Vector<>(nAxis);
        state.storedAxis = new Vector<>(nAxis);
        for (int i = 0; i < nAxis; i++)
        {
            state.axis.add(0.0f);
            state.storedAxis.add(0.0f);
        }
    }

    public static boolean IsJoystickPresent(int deviceId)
    {
        return deviceId >= 0 && deviceId < MAX_JOYSTICKS && devices[deviceId] != null;
    }

    public static List<Integer> GetPresentDevices()
    {
        return Collections.unmodifiableList(presentDevices);
    }

    public static int GetButtonCount(int deviceId)
    {
        return IsJoystickPresent(deviceId) ? devices[deviceId].buttons.size() : 0;
    }

    public static int GetAxisCount(int deviceId)
    {
        return IsJoystickPresent(deviceId) ? devices[deviceId].axis.size() : 0;
    }

    public static boolean GetButton(int deviceId, int button)
    {
        return button < 0 || button >= GetButtonCount(deviceId) ? false : devices[deviceId].buttons.get(button);
    }

    public static float GetAxis(int deviceId, int axis)
    {
        return axis < 0 || axis >= GetAxisCount(deviceId) ? 0.0f : devices[deviceId].axis.get(axis);
    }

    // ---- Single-device (backwards compatible) API, delegating to the primary device ----

    public static int GetButtonCount()
    {
        return HasJoystick() ? devices[selectedJoystick].buttons.size() : 0;
    }

    public static int GetAxisCount()
    {
        return HasJoystick() ? devices[selectedJoystick].axis.size() : 0;
    }

    public static int GetJoystickIndex()
    {
        return selectedJoystick;
    }

    public static boolean HasJoystick()
    {
        return selectedJoystick >= 0;
    }

    public static void SearchGamepad()
    {
        selectedJoystick = -1;
    }

    public static boolean GetButton(int button)
    {
        return GetButton(selectedJoystick, button);
    }

    public static float GetAxis(int axis)
    {
        return GetAxis(selectedJoystick, axis);
    }

    private static void Fill(DeviceState state, ByteBuffer b, FloatBuffer a)
    {
        if (b != null)
        {
            for (int i = 0; i < b.limit(); i++)
            {
                state.buttons.set(i, b.get(i) == GLFW.GLFW_PRESS);
            }
        }
        if (a != null)
        {
            for (int i = 0; i < a.limit(); i++)
            {
                state.axis.set(i, a.get(i));
            }
        }
    }

    // ---- Per-device stored-value accessors for the binding UI ----

    public static void StoreAxisValues(int deviceId)
    {
        if (!IsJoystickPresent(deviceId)) return;
        DeviceState s = devices[deviceId];
        for (int i = 0; i < s.axis.size(); i++)
        {
            s.storedAxis.set(i, s.axis.get(i));
        }
    }

    public static void StoreButtonsValues(int deviceId)
    {
        if (!IsJoystickPresent(deviceId)) return;
        DeviceState s = devices[deviceId];
        for (int i = 0; i < s.buttons.size(); i++)
        {
            s.storedButtons.set(i, s.buttons.get(i));
        }
    }

    public static int GetFirstButton(int deviceId)
    {
        if (!IsJoystickPresent(deviceId)) return -1;
        DeviceState s = devices[deviceId];
        for (int i = 0; i < s.buttons.size(); i++)
        {
            if (s.buttons.get(i) != s.storedButtons.get(i)) return i;
        }
        return -1;
    }

    public static int GetFirstAxis(int deviceId)
    {
        if (!IsJoystickPresent(deviceId)) return -1;
        DeviceState s = devices[deviceId];
        for (int i = 0; i < s.axis.size(); i++)
        {
            if (Math.abs(s.axis.get(i) - s.storedAxis.get(i)) > AXIS_CHANGE_THRESHOLD) return i;
        }
        return -1;
    }

    public static float GetStoredAxis(int deviceId, int index)
    {
        if (!IsJoystickPresent(deviceId)) return 0.0f;
        DeviceState s = devices[deviceId];
        return index < 0 || index >= s.storedAxis.size() ? 0.0f : s.storedAxis.get(index);
    }

    public static boolean GetStoredButton(int deviceId, int index)
    {
        if (!IsJoystickPresent(deviceId)) return false;
        DeviceState s = devices[deviceId];
        return index >= 0 && index < s.storedButtons.size() ? s.storedButtons.get(index) : false;
    }

    // ---- Single-device (backwards compatible) stored-value accessors ----

    public static void StoreAxisValues()
    {
        StoreAxisValues(selectedJoystick);
    }

    public static void StoreButtonsValues()
    {
        StoreButtonsValues(selectedJoystick);
    }

    public static int GetFirstButton()
    {
        return GetFirstButton(selectedJoystick);
    }

    public static int GetFirstAxis()
    {
        return GetFirstAxis(selectedJoystick);
    }

    public static float GetStoredAxis(int index)
    {
        return GetStoredAxis(selectedJoystick, index);
    }

    public static boolean GetStoredButton(int index)
    {
        return GetStoredButton(selectedJoystick, index);
    }
}
