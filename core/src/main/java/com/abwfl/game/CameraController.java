package com.abwfl.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.math.MathUtils.*;

public class CameraController {
    public Camera camera;
    private final World world = new World();

    private final Vector3 position = new Vector3(7f,0.1f,-4f);

    float moveSpeed = 40f;

    public float playerHeight = .6f;
    float colliderSize = .2f;

    private float rotSpeed = 200f;

    private final Vector3 moveVector = new Vector3();
    private final Vector3 tmpVector = new Vector3();

    public CameraController(Camera cam, int sens){
        camera = cam;

        rotSpeed *= sens;
    }

    public void update() {

        float dt = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Gdx.input.setCursorCatched(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.rotate(Vector3.Y, rotSpeed * dt);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.rotate(Vector3.Y, -rotSpeed * dt);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (camera.direction.y < 30 * degRad) {
                camera.direction.y += 3 * degRad;
            } else {
                camera.direction.y = 30 * degRad;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (camera.direction.y > -30 * degRad) {
                camera.direction.y -= 3 * degRad;
            } else {
                camera.direction.y = -30 * degRad;
            }
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.UP) == !Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (round(camera.direction.y * radDeg) == 0) {
                camera.direction.y = 0;
            } else if (camera.direction.y > 0) {
                camera.direction.y -= 3 * degRad;
            } else {
                camera.direction.y += 3 * degRad;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            if (round(playerHeight*10) == 40) {
                playerHeight = .4f;
            } else if (playerHeight > .4f) {
                playerHeight -= .05f;
            }
        } else {
            if (round(playerHeight*10) == 60) {
                playerHeight = .6f;
            } else if (playerHeight < .6f) {
                playerHeight += .05f;
            }
        }

        move();
    }

    private void move(){
        float dt = Gdx.graphics.getDeltaTime();

        moveVector.setZero();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            tmpVector.set(camera.direction);
            tmpVector.y = 0;
            moveVector.add(tmpVector.nor().scl(dt * moveSpeed));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            tmpVector.set(camera.direction);
            tmpVector.y = 0;
            moveVector.add(tmpVector.nor().scl(dt * -moveSpeed));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            tmpVector.set(camera.direction).crs(camera.up);
            tmpVector.y = 0;
            moveVector.add(tmpVector.nor().scl(dt * -moveSpeed));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            tmpVector.set(camera.direction).crs(camera.up);
            tmpVector.y = 0;
            moveVector.add(tmpVector.nor().scl(dt * moveSpeed));
        }

        float colX = moveVector.nor().x/moveSpeed==0 ? 0 : (moveVector.nor().x/moveSpeed>0 ? 1 : -1);
        float colZ = moveVector.nor().z/moveSpeed==0 ? 0 : (moveVector.nor().z/moveSpeed>0 ? 1 : -1);

        if (world.getCollision(position.x + moveVector.nor().x/moveSpeed + colX * colliderSize, position.z+colliderSize) == 0)
            if (world.getCollision(position.x + moveVector.nor().x/moveSpeed + colX * colliderSize, position.z-colliderSize) == 0)
                position.add(moveVector.nor().x/moveSpeed, 0, 0);
        if (world.getCollision(position.x+colliderSize, position.z + moveVector.nor().z/moveSpeed + colZ * colliderSize) == 0)
            if (world.getCollision(position.x-colliderSize, position.z + moveVector.nor().z/moveSpeed + colZ * colliderSize) == 0)
                position.add(0, 0, moveVector.nor().z/moveSpeed);

        position.y = world.getHeight(position.x,position.z+19);

        camera.position.set(position.x, position.y + playerHeight, position.z);
    }
}
