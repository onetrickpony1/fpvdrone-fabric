package com.fpvdrone.client;

import org.joml.Vector3f;

public class Transforms {
    private static final float RADS = (float) (Math.PI / 180.0);
    private static final float DEGS = (float) (180.0 / Math.PI);

    public static float bfRate(float rcCommand, float rcRate, float superRate, float expo) {
        float absRcCommand = Math.abs(rcCommand);
        if (rcRate > 2.0F) {
            rcRate += 14.54F * (rcRate - 2.0F);
        }

        if (expo != 0.0F) {
            rcCommand = rcCommand * (float)Math.pow(absRcCommand, 3.0) * expo + rcCommand * (1.0F - expo);
        }

        float angleRate = 200.0F * rcRate * rcCommand;
        if (superRate != 0.0F) {
            float rcSuperFactor = 1.0F / clamp(1.0F - absRcCommand * superRate, 0.01F, 1.0F);
            angleRate *= rcSuperFactor;
        }

        return angleRate;
    }

    public static float[] getWorldEulerAngles(Vector3f forward, Vector3f up) {
        float worldYaw = 0.0F;
        float worldPitch = 0.0F;
        float worldRoll = 0.0F;
        if (Math.abs(forward.x) > 1.0E-4F || Math.abs(forward.z) > 1.0E-4F) {
            Vector3f forwardProj = new Vector3f(forward.x, 0.0F, forward.z);
            if (forwardProj.lengthSquared() > 1.0E-4F) {
                forwardProj.normalize();
            }

            float[] yawAxisAngle = lookAtToAxisAngle(forwardProj, new Vector3f(0.0F, 1.0F, 0.0F));
            worldYaw = yawAxisAngle[0] * yawAxisAngle[2] * (float) (180.0 / Math.PI);
            float[] antiYawQ = axisAngleToQuat(-worldYaw * (float) (Math.PI / 180.0), 0.0F, 1.0F, 0.0F);
            Vector3f rotatedForward = quatMult(antiYawQ, forward);
            float[] pitchAxisAngle = lookAtToAxisAngle(rotatedForward, new Vector3f(0.0F, 1.0F, 0.0F));
            worldPitch = pitchAxisAngle[0] * pitchAxisAngle[1] * (float) (180.0 / Math.PI);
            float[] rollessQ = fromAngles(worldPitch * (float) (Math.PI / 180.0), worldYaw * (float) (Math.PI / 180.0), 0.0F);
            Vector3f rollessUp = quatMult(rollessQ, new Vector3f(0.0F, 1.0F, 0.0F));
            Vector3f crossUps = cross(rollessUp, up);
            if (crossUps.lengthSquared() > 1.0E-4F) {
                crossUps.normalize();
            }

            float lookAngle = angleBetween(crossUps, forward) * (float) (180.0 / Math.PI);
            float flip = Math.abs(lookAngle) > 90.0F ? -1.0F : 1.0F;
            worldRoll = angleBetween(rollessUp, up) * (float) (180.0 / Math.PI) * flip;
        } else if (forward.y > 0.0F) {
            worldYaw = 0.0F;
            worldPitch = -90.0F;
            Vector3f upProj = new Vector3f(up.x, 0.0F, up.z);
            if (upProj.lengthSquared() > 0.001F) {
                upProj.normalize();
                float[] axisAngle = lookAtToAxisAngle(upProj, new Vector3f(0.0F, 1.0F, 0.0F));
                worldRoll = axisAngle[0] * axisAngle[2] * (float) (180.0 / Math.PI);
            }
        } else if (forward.y < 0.0F) {
            worldYaw = 0.0F;
            worldPitch = 90.0F;
            Vector3f upProj = new Vector3f(up.x, 0.0F, up.z);
            if (upProj.lengthSquared() > 0.001F) {
                upProj.normalize();
                float[] axisAngle = lookAtToAxisAngle(upProj, new Vector3f(0.0F, 1.0F, 0.0F));
                worldRoll = axisAngle[0] * axisAngle[2] * (float) (180.0 / Math.PI);
            }
        }

        return new float[]{-worldYaw, worldPitch, worldRoll};
    }

    private static float[] lookAtToAxisAngle(Vector3f direction, Vector3f up) {
        Vector3f zAxis = new Vector3f(direction).normalize();
        Vector3f xAxis = cross(up, zAxis);
        if (xAxis.lengthSquared() < 1.0E-4F) {
            xAxis = new Vector3f(1.0F, 0.0F, 0.0F);
        } else {
            xAxis.normalize();
        }

        Vector3f yAxis = cross(zAxis, xAxis);
        float trace = xAxis.x + yAxis.y + zAxis.z;
        float angle;
        float x;
        float y;
        float z;
        if (trace > 0.0F) {
            float s = (float)Math.sqrt(trace + 1.0F) * 2.0F;
            float w = 0.25F * s;
            x = (yAxis.z - zAxis.y) / s;
            y = (zAxis.x - xAxis.z) / s;
            z = (xAxis.y - yAxis.x) / s;
            angle = 2.0F * (float)Math.acos(clamp(w, -1.0F, 1.0F));
        } else if (xAxis.x > yAxis.y && xAxis.x > zAxis.z) {
            float s = (float)Math.sqrt(1.0F + xAxis.x - yAxis.y - zAxis.z) * 2.0F;
            x = 0.25F * s;
            y = (yAxis.x + xAxis.y) / s;
            z = (zAxis.x + xAxis.z) / s;
            float w = (yAxis.z - zAxis.y) / s;
            angle = 2.0F * (float)Math.acos(clamp(w, -1.0F, 1.0F));
        } else if (yAxis.y > zAxis.z) {
            float s = (float)Math.sqrt(1.0F + yAxis.y - xAxis.x - zAxis.z) * 2.0F;
            x = (yAxis.x + xAxis.y) / s;
            y = 0.25F * s;
            z = (zAxis.y + yAxis.z) / s;
            float w = (zAxis.x - xAxis.z) / s;
            angle = 2.0F * (float)Math.acos(clamp(w, -1.0F, 1.0F));
        } else {
            float s = (float)Math.sqrt(1.0F + zAxis.z - xAxis.x - yAxis.y) * 2.0F;
            x = (zAxis.x + xAxis.z) / s;
            y = (zAxis.y + yAxis.z) / s;
            z = 0.25F * s;
            float w = (xAxis.y - yAxis.x) / s;
            angle = 2.0F * (float)Math.acos(clamp(w, -1.0F, 1.0F));
        }

        float len = (float)Math.sqrt(x * x + y * y + z * z);
        if (len > 1.0E-4F) {
            x /= len;
            y /= len;
            z /= len;
        }

        return new float[]{angle, x, y, z};
    }

    private static float[] axisAngleToQuat(float angle, float ax, float ay, float az) {
        float halfAngle = angle * 0.5F;
        float sin = (float)Math.sin(halfAngle);
        return new float[]{ax * sin, ay * sin, az * sin, (float)Math.cos(halfAngle)};
    }

    private static float[] fromAngles(float pitch, float yaw, float roll) {
        float halfPitch = pitch * 0.5F;
        float halfYaw = yaw * 0.5F;
        float halfRoll = roll * 0.5F;
        float sinP = (float)Math.sin(halfPitch);
        float cosP = (float)Math.cos(halfPitch);
        float sinY = (float)Math.sin(halfYaw);
        float cosY = (float)Math.cos(halfYaw);
        float sinR = (float)Math.sin(halfRoll);
        float cosR = (float)Math.cos(halfRoll);
        float w = cosR * cosP * cosY + sinR * sinP * sinY;
        float x = cosR * sinP * cosY + sinR * cosP * sinY;
        float y = cosR * cosP * sinY - sinR * sinP * cosY;
        float z = sinR * cosP * cosY - cosR * sinP * sinY;
        return new float[]{x, y, z, w};
    }

    private static Vector3f quatMult(float[] q, Vector3f v) {
        float qx = q[0];
        float qy = q[1];
        float qz = q[2];
        float qw = q[3];
        float x = qw * qw * v.x
            + 2.0F * qy * qw * v.z
            - 2.0F * qz * qw * v.y
            + qx * qx * v.x
            + 2.0F * qy * qx * v.y
            + 2.0F * qz * qx * v.z
            - qz * qz * v.x
            - qy * qy * v.x;
        float y = 2.0F * qx * qy * v.x
            + qy * qy * v.y
            + 2.0F * qz * qy * v.z
            + 2.0F * qw * qz * v.x
            - qz * qz * v.y
            + qw * qw * v.y
            - 2.0F * qx * qw * v.z
            - qx * qx * v.y;
        float z = 2.0F * qx * qz * v.x
            + 2.0F * qy * qz * v.y
            + qz * qz * v.z
            - 2.0F * qw * qy * v.x
            - qy * qy * v.z
            + 2.0F * qw * qx * v.y
            - qx * qx * v.z
            + qw * qw * v.z;
        return new Vector3f(x, y, z);
    }

    private static Vector3f cross(Vector3f a, Vector3f b) {
        return new Vector3f(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }

    private static float angleBetween(Vector3f a, Vector3f b) {
        float dot = a.x * b.x + a.y * b.y + a.z * b.z;
        dot = clamp(dot, -1.0F, 1.0F);
        return (float)Math.acos(dot);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
