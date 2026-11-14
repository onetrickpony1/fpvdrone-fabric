package com.fpvdrone.physics;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.Transforms;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DefaultPhysicsCore implements IPhysicsCore {
    private static final float RADS = (float) (Math.PI / 180.0);
    private Vector3f velocity = new Vector3f(0.0F, 0.0F, 0.0F);
    private Vector3f droneLook = new Vector3f(0.0F, 0.0F, 1.0F);
    private Vector3f droneUp = new Vector3f(0.0F, 1.0F, 0.0F);
    private Vector3f droneLeft = new Vector3f(1.0F, 0.0F, 0.0F);
    private float mass = 1.05F;
    private float motorKv = 1300.0F;
    private int batteryCells = 6;
    private float propDiameter = 0.2286F;
    private float propPitch = 0.1143F;
    private float dragCoefficient = 1.1F;
    private boolean flightMode3d = false;
    private float thrustMultiplier = 1.0F;
    private float reverseThrustFactor = 0.65F;
    private boolean motorsArmed = false;
    private float kickYaw = 0.0F;
    private float kickPitch = 0.0F;
    private float kickRoll = 0.0F;
    private static final float KICK_DECAY = 0.85F;
    private float smoothYawRate = 0.0F;
    private float smoothPitchRate = 0.0F;
    private float smoothRollRate = 0.0F;
    private static final float ANGULAR_INERTIA_TAU = 0.04F;
    private float[] rcRate = new float[]{1.0F, 1.15F, 1.15F, 1.15F};
    private float[] superRate = new float[]{0.7F, 0.67F, 0.67F, 0.67F};
    private float[] expo = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
    private boolean falling = false;
    private float fallingThrustFactor = 0.0F;
    private float fallingDragMultiplier = 1.0F;
    private float angVelYaw = 0.0F;
    private float angVelPitch = 0.0F;
    private float angVelRoll = 0.0F;
    private static final float DRONE_DRAG_AREA = 0.196F;
    private static final float DRONE_DRAG_CD = 1.1F;
    private static final float CELL_VOLTAGE = 3.7F;
    private PhysicsState physicsState;

    public DefaultPhysicsCore(float mass, float motorKv, float propDiameter, float dragCoefficient, int batteryCells) {
        this.mass = mass;
        this.motorKv = motorKv;
        this.propDiameter = propDiameter;
        this.dragCoefficient = dragCoefficient;
        this.batteryCells = batteryCells;
    }

    public void setPhysicsState(PhysicsState state) {
        this.physicsState = state;
    }

    public void setRcRate(int ch, float v) {
        if (ch >= 0 && ch < 4) {
            this.rcRate[ch] = v;
        }
    }

    public void setSuperRate(int ch, float v) {
        if (ch >= 0 && ch < 4) {
            this.superRate[ch] = v;
        }
    }

    public void setExpo(int ch, float v) {
        if (ch >= 0 && ch < 4) {
            this.expo[ch] = v;
        }
    }

    @Override
    public void step(float dt) {
        if (this.falling) {
            this.fallingStep(dt);
        } else {
            this.staticStep(dt);
            boolean throttleCut = this.physicsState != null && this.physicsState.isThrottleCut();
            if (this.physicsState != null && this.physicsState.isGroundContact() && !throttleCut) {
                this.levelDrone(dt);
            } else if (this.motorsArmed) {
                this.rotateDrone(dt);
                if (this.physicsState != null && this.isCameraInsideBlock()) {
                    this.levelDrone(dt);
                }
            }
        }
    }

    private boolean isCameraInsideBlock() {
        if (this.physicsState == null) {
            return false;
        } else {
            float camFwd = 0.30889937F;
            float camUp = 0.22122687F;
            double cx = this.physicsState.getPlayerX() + this.droneUp.x * camUp + this.droneLook.x * camFwd;
            double cy = this.physicsState.getPlayerY() + this.droneUp.y * camUp + this.droneLook.y * camFwd;
            double cz = this.physicsState.getPlayerZ() + this.droneUp.z * camUp + this.droneLook.z * camFwd;
            return this.physicsState.isPointInsideSolidBlock(cx, cy, cz);
        }
    }

    private void levelDrone(float dt) {
        Vector3f worldUp = new Vector3f(0.0F, 1.0F, 0.0F);
        float levelSpeed = 12.0F;
        float t = Math.min(1.0F, levelSpeed * dt);
        this.droneUp.lerp(worldUp, t);
        this.droneUp.normalize();
        this.droneLeft = cross(this.droneUp, this.droneLook);
        if (this.droneLeft.lengthSquared() < 1.0E-4F) {
            this.droneLeft = cross(this.droneUp, new Vector3f(0.0F, 0.0F, 1.0F));
        }

        this.droneLeft.normalize();
        this.droneLook = cross(this.droneLeft, this.droneUp);
        this.droneLook.normalize();
    }

    private void fallingStep(float dt) {
        float yawDiff = this.angVelYaw * dt * (float) (Math.PI / 180.0);
        float pitchDiff = this.angVelPitch * dt * (float) (Math.PI / 180.0);
        float rollDiff = this.angVelRoll * dt * (float) (Math.PI / 180.0);
        Quaternionf yawRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneUp), -yawDiff);
        yawRot.transform(this.droneLook);
        yawRot.transform(this.droneLeft);
        Quaternionf pitchRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), pitchDiff);
        pitchRot.transform(this.droneLook);
        pitchRot.transform(this.droneUp);
        Quaternionf rollRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), rollDiff);
        rollRot.transform(this.droneUp);
        rollRot.transform(this.droneLeft);
        this.droneLook.normalize();
        this.droneUp.normalize();
        this.droneLeft.normalize();
        this.droneLeft = cross(this.droneUp, this.droneLook);
        this.droneLeft.normalize();
        Vector3f gravity = PhysicsState.getGravity();
        float dragFactor = 0.132055F;
        dragFactor *= this.fallingDragMultiplier;
        float voltage = this.batteryCells * 3.7F;
        float baseThrustPerMotor = 2.04F * (this.propDiameter / 0.2286F) * (this.propPitch / 0.1143F) * (this.motorKv / 1300.0F) * (voltage / 22.2F);
        float maxTotalThrust = baseThrustPerMotor * 4.0F;
        float maxThrust = maxTotalThrust / this.mass * 9.80665F * this.thrustMultiplier;
        float maxTimeStep = 0.0078125F;
        int maxIterations = (int)(dt / maxTimeStep) + 10;
        float timeProcessed = 0.0F;

        for (int i = 0; i < maxIterations && dt - timeProcessed > 1.0E-5F; i++) {
            float remainingTime = dt - timeProcessed;
            float timeStep = Math.min(maxTimeStep, remainingTime);
            float speed = this.velocity.length();
            Vector3f velNorm = speed > 1.0E-4F ? new Vector3f(this.velocity).normalize() : new Vector3f(0.0F, 0.0F, 0.0F);
            Vector3f ambientDragForce = new Vector3f(velNorm).mul(-speed * speed * dragFactor);
            Vector3f gravityForce = new Vector3f(gravity).mul(this.mass);
            Vector3f netForce = new Vector3f(gravityForce).add(ambientDragForce);
            float thrustAccel = this.fallingThrustFactor * maxThrust;
            Vector3f thrust = new Vector3f(this.droneUp).mul(thrustAccel);
            Vector3f acceleration = new Vector3f(netForce).div(this.mass);
            acceleration.add(thrust);
            Vector3f nextVelocity = new Vector3f(this.velocity).add(new Vector3f(acceleration).mul(timeStep));
            if (this.physicsState != null) {
                this.velocity = this.physicsState.collideAndMove(nextVelocity, timeStep);
            } else {
                this.velocity = nextVelocity;
            }

            float limit = 500.0F;
            if (this.velocity.length() > limit) {
                this.velocity.normalize().mul(limit);
            }

            if (Float.isNaN(this.velocity.x) || Float.isNaN(this.velocity.y) || Float.isNaN(this.velocity.z)) {
                this.velocity.set(0.0F, 0.0F, 0.0F);
            }

            timeProcessed += timeStep;
        }
    }

    private void staticStep(float dt) {
        Vector3f gravity = PhysicsState.getGravity();
        float throttle = this.motorsArmed ? ControllerReader.getThrottle() : -1.0F;
        if (this.physicsState != null && this.physicsState.isThrottleCut()) {
            throttle = -1.0F;
        }

        float dragFactor = 0.132055F;
        boolean isReverse = false;
        float throt;
        if (this.flightMode3d) {
            throt = clamp(throttle, -1.0F, 1.0F);
            isReverse = throt < 0.0F;
            throt = Math.abs(throt);
        } else {
            throt = clamp((throttle + 1.0F) * 0.5F, 0.0F, 1.0F);
        }

        float maxTimeStep = 0.0078125F;
        int maxIterations = (int)(dt / maxTimeStep) + 10;
        float timeProcessed = 0.0F;

        for (int i = 0; i < maxIterations && dt - timeProcessed > 1.0E-5F; i++) {
            float remainingTime = dt - timeProcessed;
            float timeStep = Math.min(maxTimeStep, remainingTime);
            boolean throttleCutNow = this.physicsState != null && this.physicsState.isThrottleCut();
            float subThrot = throttleCutNow ? 0.0F : throt;
            boolean subReverse = throttleCutNow ? false : isReverse;
            float speed = this.velocity.length();
            Vector3f velNorm = speed > 1.0E-4F ? new Vector3f(this.velocity).normalize() : new Vector3f(0.0F, 0.0F, 0.0F);
            Vector3f ambientDragForce = new Vector3f(velNorm).mul(-speed * speed * dragFactor);
            Vector3f gravityForce = new Vector3f(gravity).mul(this.mass);
            Vector3f netForce = new Vector3f(gravityForce).add(ambientDragForce);
            float voltage = this.batteryCells * 3.7F;
            float baseThrustPerMotor = 2.04F * (this.propDiameter / 0.2286F) * (this.propPitch / 0.1143F) * (this.motorKv / 1300.0F) * (voltage / 22.2F);
            float maxTotalThrust = baseThrustPerMotor * 4.0F;
            float currentThrust = maxTotalThrust * subThrot;
            float thrustAccel = currentThrust / this.mass * 9.80665F * this.thrustMultiplier;
            if (subReverse) {
                thrustAccel *= this.reverseThrustFactor;
            }

            Vector3f thrustDir = subReverse ? new Vector3f(this.droneUp).negate() : new Vector3f(this.droneUp);
            Vector3f thrust = new Vector3f(thrustDir).mul(thrustAccel);
            Vector3f acceleration = new Vector3f(netForce).div(this.mass);
            acceleration.add(thrust);
            Vector3f nextVelocity = new Vector3f(this.velocity).add(new Vector3f(acceleration).mul(timeStep));
            if (this.physicsState != null) {
                this.velocity = this.physicsState.collideAndMove(nextVelocity, timeStep);
            } else {
                this.velocity = nextVelocity;
            }

            float limit = 500.0F;
            if (this.velocity.length() > limit) {
                this.velocity.normalize().mul(limit);
            }

            if (Float.isNaN(this.velocity.x) || Float.isNaN(this.velocity.y) || Float.isNaN(this.velocity.z)) {
                this.velocity.set(0.0F, 0.0F, 0.0F);
            }

            timeProcessed += timeStep;
        }
    }

    private void rotateDrone(float elapsed) {
        boolean throttleCut = this.physicsState != null && this.physicsState.isThrottleCut();
        boolean ignoreSticks = throttleCut || !this.motorsArmed;
        float yawInput;
        float pitchInput;
        float rollInput;
        if (ControllerReader.getInstance().isPhysicalControllerConnected()) {
            yawInput = ignoreSticks ? 0.0F : ControllerReader.getYaw();
            pitchInput = ignoreSticks ? 0.0F : ControllerReader.getPitch();
            rollInput = ignoreSticks ? 0.0F : ControllerReader.getRoll();
        } else if (this.physicsState != null && !ignoreSticks) {
            float yawDelta = this.physicsState.getMouseDeltaYaw();
            float pitchDelta = this.physicsState.getMouseDeltaPitch();
            this.physicsState.clearMouseDelta();
            float mouseSensitivity = 0.067F;
            pitchInput = Mth.clamp(pitchDelta * mouseSensitivity, -1.0F, 1.0F);
            rollInput = Mth.clamp(yawDelta * mouseSensitivity, -1.0F, 1.0F);
            yawInput = ControllerReader.getYaw();
        } else {
            yawInput = 0.0F;
            pitchInput = 0.0F;
            rollInput = 0.0F;
        }

        float yawRate = Transforms.bfRate(yawInput, this.rcRate[1], this.superRate[1], this.expo[1]);
        float pitchRate = Transforms.bfRate(pitchInput, this.rcRate[2], this.superRate[2], this.expo[2]);
        float rollRate = Transforms.bfRate(rollInput, this.rcRate[3], this.superRate[3], this.expo[3]);
        float alpha = elapsed / (elapsed + 0.04F);
        this.smoothYawRate = this.smoothYawRate + alpha * (yawRate - this.smoothYawRate);
        this.smoothPitchRate = this.smoothPitchRate + alpha * (pitchRate - this.smoothPitchRate);
        this.smoothRollRate = this.smoothRollRate + alpha * (rollRate - this.smoothRollRate);
        float massFactor = (float)Math.sqrt(1.05F / this.mass);
        float yawDiff = this.smoothYawRate * massFactor * elapsed * (float) (Math.PI / 180.0);
        float pitchDiff = this.smoothPitchRate * massFactor * elapsed * (float) (Math.PI / 180.0);
        float rollDiff = this.smoothRollRate * massFactor * elapsed * (float) (Math.PI / 180.0);
        if (Math.abs(this.kickYaw) > 0.01F || Math.abs(this.kickPitch) > 0.01F || Math.abs(this.kickRoll) > 0.01F) {
            yawDiff += this.kickYaw * elapsed * (float) (Math.PI / 180.0);
            pitchDiff += this.kickPitch * elapsed * (float) (Math.PI / 180.0);
            rollDiff += this.kickRoll * elapsed * (float) (Math.PI / 180.0);
            float decay = throttleCut ? 0.96F : 0.85F;
            this.kickYaw *= decay;
            this.kickPitch *= decay;
            this.kickRoll *= decay;
        }

        Quaternionf yawRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneUp), -yawDiff);
        yawRot.transform(this.droneLook);
        yawRot.transform(this.droneLeft);
        Vector3f prePitchLook = new Vector3f(this.droneLook);
        Vector3f prePitchUp = new Vector3f(this.droneUp);
        Quaternionf pitchRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLeft), pitchDiff);
        pitchRot.transform(this.droneLook);
        pitchRot.transform(this.droneUp);
        if (this.physicsState != null && this.isCameraInsideBlock()) {
            this.droneLook.set(prePitchLook);
            this.droneUp.set(prePitchUp);
        }

        Quaternionf rollRot = new Quaternionf().fromAxisAngleRad(new Vector3f(this.droneLook), rollDiff);
        rollRot.transform(this.droneUp);
        rollRot.transform(this.droneLeft);
        this.droneLook.normalize();
        this.droneUp.normalize();
        this.droneLeft.normalize();
        this.droneLeft = cross(this.droneUp, this.droneLook);
        this.droneLeft.normalize();
    }

    private static Vector3f cross(Vector3f a, Vector3f b) {
        float x = a.y * b.z - a.z * b.y;
        float y = a.z * b.x - a.x * b.z;
        float z = a.x * b.y - a.y * b.x;
        return new Vector3f(x, y, z);
    }

    @Override
    public Vector3f getVelocity() {
        return this.velocity;
    }

    @Override
    public void setVelocity(Vector3f velocity) {
        this.velocity = new Vector3f(velocity);
    }

    @Override
    public Vector3f getDroneLook() {
        return this.droneLook;
    }

    @Override
    public void setDroneLook(Vector3f look) {
        this.droneLook = new Vector3f(look);
    }

    @Override
    public Vector3f getDroneUp() {
        return this.droneUp;
    }

    @Override
    public void setDroneUp(Vector3f up) {
        this.droneUp = new Vector3f(up);
    }

    @Override
    public Vector3f getDroneLeft() {
        return this.droneLeft;
    }

    @Override
    public void setDroneLeft(Vector3f left) {
        this.droneLeft = new Vector3f(left);
    }

    @Override
    public void reset() {
        this.velocity = new Vector3f(0.0F, 0.0F, 0.0F);
        this.droneLook = new Vector3f(0.0F, 0.0F, 1.0F);
        this.droneUp = new Vector3f(0.0F, 1.0F, 0.0F);
        this.droneLeft = new Vector3f(1.0F, 0.0F, 0.0F);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public void setMotorKv(float motorKv) {
        this.motorKv = motorKv;
    }

    public void setDragCoefficient(float dragCoefficient) {
        this.dragCoefficient = dragCoefficient;
    }

    public void setBatteryCells(int batteryCells) {
        this.batteryCells = batteryCells;
    }

    public void setFlightMode3d(boolean mode) {
        this.flightMode3d = mode;
    }

    public void setThrustMultiplier(float mult) {
        this.thrustMultiplier = mult;
    }

    public void setPropDiameter(float propDiameter) {
        this.propDiameter = propDiameter;
    }

    public void setPropPitch(float propPitch) {
        this.propPitch = propPitch;
    }

    public void setMotorsArmed(boolean armed) {
        this.motorsArmed = armed;
    }

    public boolean isMotorsArmed() {
        return this.motorsArmed;
    }

    @Override
    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    @Override
    public boolean isFalling() {
        return this.falling;
    }

    @Override
    public void setFallingParams(float thrustFactor, float dragMultiplier) {
        this.fallingThrustFactor = thrustFactor;
        this.fallingDragMultiplier = dragMultiplier;
    }

    @Override
    public void applyImpulse(Vector3f impulse) {
        this.velocity.add(impulse);
    }

    public void applyRotationalKick(float yaw, float pitch, float roll) {
        this.kickYaw += yaw;
        this.kickPitch += pitch;
        this.kickRoll += roll;
    }

    @Override
    public void setAngularVelocity(float yawRate, float pitchRate, float rollRate) {
        this.angVelYaw = yawRate;
        this.angVelPitch = pitchRate;
        this.angVelRoll = rollRate;
    }
}
