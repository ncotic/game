package com.abwfl.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.util.Arrays;

public class Main extends ApplicationAdapter {
    private Environment environment;
    private CameraController cameraController;
    private World world;
    private PerspectiveCamera camera;
    private PointLight pointLight;

    protected Stage stage;
    protected Label label;
    protected BitmapFont font;
    protected StringBuilder stringBuilder;

    @Override
    public void create() {
        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        stringBuilder = new StringBuilder();

        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0.7f, -2f);
        camera.lookAt(0f, 0.7f, 0f);
        camera.near = .1f;
        camera.far = 1000f;
        camera.update();

        world = new World();
        world.create(camera);

//        nancy = new Texture("nancy.png");
//        Image image = new Image(nancy);
//        stage.addActor(image);

        cameraController = new CameraController(camera, 1);

        environment = new Environment();
        //environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 0.1f));
        environment.add(pointLight = new PointLight().set(1, 1, 1, 0f, 0f, 0f, 3f));
    }

    @Override
    public void resize(int width, int height) {
        if (width/height < 2) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
        }
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render() {
        camera.update();
        cameraController.update();
        world.update(camera);
        pointLight.position.set(camera.position);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        world.modelBatch.begin(camera);
        // int visibleCount = 0;
        for (final World.GameObject instance : world.instances) {
            if (instance.isVisible(camera)) {
                world.modelBatch.render(instance, environment);
                // visibleCount++;
            }
        }
        world.modelBatch.end();

        for (int i = 0; i < World.decals.size; i++) {
            Decal decal = World.decals.get(i).decal;
            decal.lookAt(new Vector3(camera.position.x, decal.getPosition().y, camera.position.z), camera.up);
            world.decalBatch.add(decal);
        }
        world.decalBatch.flush();

        stringBuilder.setLength(0);
//        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
//        stringBuilder.append(" Visible: ").append(visibleCount);
        label.setText(stringBuilder);
        stage.act();
        stage.draw();
    }

    @Override
    public void dispose() {
        world.modelBatch.dispose();
        world.decalBatch.dispose();
        world.instances.clear();
        World.decals.clear();
        world.assets.dispose();
    }
}
