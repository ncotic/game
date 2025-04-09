package com.abwfl.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.math.MathUtils.lerp;

public class World {
    private int[][] building = {{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                                {0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 1, 1, 2, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 1, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                                {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    private float[][] ground = {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, -.25f, 0, 0, 0, .2f, .2f, 0, 0, 0, 0, 0, 0, 0},
                                {0, -.25f, -.5f, -.25f, 0, .2f, .5f, .5f, .2f, 0, 0, 0, 0, 0, 0},
                                {0, 0, -.25f, 0, 0, .2f, .5f, .5f, .2f, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, .2f, .2f, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    public ModelBatch modelBatch;
    public DecalBatch decalBatch;
    public AssetManager assets;
    public final Array<GameObject> instances = new Array<>();
    public final Array<Decal> decals = new Array<>();

    public Model space;

    private Texture brickTex;
    private Texture concreteTex;
    private Texture grassTex;
    private Texture spaceTex;
    private Texture suzieTex;

    private Decal suzie;

    private float distance = 0f;

    private Vector3 position = new Vector3();

    public int getObject (Camera camera) {
        Ray ray = camera.getPickRay(0, 0);

        int result = -1;
        float distance = -1;

        for (int i = 0; i < instances.size; ++i) {
            final World.GameObject instance = instances.get(i);

            instance.transform.getTranslation(position);
            position.add(instance.center);

            float dist2 = ray.origin.dst2(position);
            if (distance >= 0f && dist2 > distance)
                continue;

            if (Intersector.intersectRaySphere(ray, position, instance.radius, null)) {
                result = i;
                distance = dist2;
            }
        }

        return result;
    }

    public static class GameObject extends ModelInstance {
        public final Vector3 center = new Vector3();
        public final Vector3 dimensions = new Vector3();
        public final float radius;

        private final static BoundingBox bounds = new BoundingBox();
        private final static Vector3 position = new Vector3();

        public GameObject (Model model) {
            super(model);
            calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }

        public boolean isVisible(Camera cam) {
            return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
        }
    }

    public void create(Camera camera) {
        String vert = Gdx.files.internal("shaders/test.vertex.glsl").readString();
        String frag = Gdx.files.internal("shaders/test.fragment.glsl").readString();

        modelBatch = new ModelBatch(vert, frag);
        decalBatch = new DecalBatch(new CameraGroupStrategy(camera));
        assets = new AssetManager();

        brickTex = new Texture("brick.png");
        concreteTex = new Texture("concrete.png");
        grassTex = new Texture("grass.png");
        spaceTex = new Texture("space.png");
        suzieTex = new Texture("suzie.png");
        TextureRegion suzieTexRegion = new TextureRegion(suzieTex);

        suzie = Decal.newDecal(suzieTexRegion, true);
        suzie.setPosition(8,0.375f,-1);
        suzie.setScale(0.015f);
        decals.add(suzie);

        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        ModelBuilder modelBuilder = new ModelBuilder();
        space = modelBuilder.createSphere(-300, -300, -300, 16, 8, new Material(TextureAttribute.createEmissive(spaceTex)), attr);
        instances.add(new GameObject(space));

        for (int y = 0; y < building.length; y++) {
            for (int x = 0; x < building[y].length; x++) {
                if (building[y][x] != 0) {
                    modelBuilder.begin();
                    MeshPartBuilder meshPartBuilder = modelBuilder.part("floof", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(concreteTex)));
                    meshPartBuilder.rect(x, building[y][x], y, x, building[y][x], y+0.5f, x-0.5f, building[y][x], y+0.5f, x-0.5f, building[y][x], y, 0, -1, 0);
                    meshPartBuilder.rect(x+0.5f, building[y][x], y, x+0.5f, building[y][x], y+0.5f, x, building[y][x], y+0.5f, x, building[y][x], y, 0, -1, 0);
                    meshPartBuilder.rect(x+0.5f, building[y][x], y-0.5f, x+0.5f, building[y][x], y, x, building[y][x], y, x, building[y][x], y-0.5f, 0, -1, 0);
                    meshPartBuilder.rect(x, building[y][x], y-0.5f, x, building[y][x], y, x-0.5f, building[y][x], y, x-0.5f, building[y][x], y-0.5f, 0, -1, 0);
                    meshPartBuilder.rect(x, 0, y, x, 0, y-0.5f, x-0.5f, 0, y-0.5f, x-0.5f, 0, y, 0, 1, 0);
                    meshPartBuilder.rect(x+0.5f, 0, y, x+0.5f, 0, y-0.5f, x, 0, y-0.5f, x, 0, y, 0, 1, 0);
                    meshPartBuilder.rect(x+0.5f, 0, y+0.5f, x+0.5f, 0, y, x, 0, y, x, 0, y+0.5f, 0, 1, 0);
                    meshPartBuilder.rect(x, 0, y+0.5f, x, 0, y, x-0.5f, 0, y, x-0.5f, 0, y+0.5f, 0, 1, 0);
                    Model floof = modelBuilder.end();
                    instances.add(new GameObject(floof));
                    if (x - 1 >= 0 && building[y][x-1] < building[y][x]) {
                        for (int i = building[y][x-1]; i < building[y][x]; i++) {
                            drawWall(x - 1, y, i, 0);
                        }
                    }
                    if (x + 1 < building[y].length && building[y][x+1] < building[y][x]) {
                        for (int i = building[y][x+1]; i < building[y][x]; i++) {
                            drawWall(x + 1, y, i, 1);
                        }
                    }
                    if (y - 1 >= 0 && building[y-1][x] < building[y][x]) {
                        for (int i = building[y-1][x]; i < building[y][x]; i++) {
                            drawWall(x, y - 1, i, 2);
                        }
                    }
                    if (y + 1 < building.length && building[y+1][x] < building[y][x]) {
                        for (int i = building[y+1][x]; i < building[y][x]; i++) {
                            drawWall(x, y + 1, i, 3);
                        }
                    }
                }
            }
        }
        for (int x = 0; x < building[0].length; x++) {
            if (building[0][x] == 0) {
                drawWall(x, 0, 0, 3);
            }
            drawWall(x, 0, 1, 3);
        }
        for (int y = -5; y < 0; y++) {
            for (int x = 0; x < 15; x++) {
                modelBuilder.begin();
                MeshPartBuilder meshPartBuilder = modelBuilder.part("outcon", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(concreteTex)));
                meshPartBuilder.rect(x, 0, y, x, 0, y-0.5f, x-0.5f, 0, y-0.5f, x-0.5f, 0, y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, 0, y, x+0.5f, 0, y-0.5f, x, 0, y-0.5f, x, 0, y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, 0, y+0.5f, x+0.5f, 0, y, x, 0, y, x, 0, y+0.5f, 0, 1, 0);
                meshPartBuilder.rect(x, 0, y+0.5f, x, 0, y, x-0.5f, 0, y, x-0.5f, 0, y+0.5f, 0, 1, 0);
                Model outcon = modelBuilder.end();
                instances.add(new GameObject(outcon));
            }
        }

        for (int y = 1; y < ground.length-1; y++) {
            for (int x = 1; x < ground[y].length-1; x++) {
                modelBuilder.begin();
                MeshPartBuilder meshPartBuilder = modelBuilder.part("grounds", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(grassTex)));
                meshPartBuilder.rect(x, ground[y][x], y, x, (ground[y][x]+ground[y-1][x])/2, y-0.5f, x-0.5f, (ground[y][x]+ground[y][x-1]+ground[y-1][x-1]+ground[y-1][x])/4, y-0.5f, x-0.5f, (ground[y][x]+ground[y][x-1])/2, y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, (ground[y][x]+ground[y][x+1])/2, y, x+0.5f, (ground[y][x]+ground[y][x+1]+ground[y-1][x+1]+ground[y-1][x])/4, y-0.5f, x, (ground[y][x]+ground[y-1][x])/2, y-0.5f, x, ground[y][x], y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, (ground[y][x]+ground[y][x+1]+ground[y+1][x+1]+ground[y+1][x])/4, y+0.5f, x+0.5f, (ground[y][x]+ground[y][x+1])/2, y, x, ground[y][x], y, x, (ground[y][x]+ground[y+1][x])/2, y+0.5f, 0, 1, 0);
                meshPartBuilder.rect(x, (ground[y][x]+ground[y+1][x])/2, y+0.5f, x, ground[y][x], y, x-0.5f, (ground[y][x]+ground[y][x-1])/2, y, x-0.5f, (ground[y][x]+ground[y][x-1]+ground[y+1][x-1]+ground[y+1][x])/4, y+0.5f, 0, 1, 0);
                Model grounds = modelBuilder.end();
                GameObject groundsobj = new GameObject(grounds);
                groundsobj.transform.trn(0f,0f,-19f);
                instances.add(groundsobj);
            }
        }
    }

    public void update(Camera camera) {
        instances.get(0).transform.rotate(0.1f, 0.0f, 0.2f,0.1f);
        double ac = Math.abs(camera.position.z - suzie.getPosition().z);
        double cb = Math.abs(camera.position.x - suzie.getPosition().x);
        distance = (float)Math.hypot(ac, cb);
        if (distance > 1.9) {
            distance = 1.9f;
        }
        suzie.setColor(-distance/2+3,-distance/2+3,-distance/2+3,1);
    }

    public int getCollision(float x, float y) {
        try {
            if (building[Math.round(y)][Math.round(x)] != 0) {
                return 0;
            } else {
                return 1;
            }
        }
        catch(ArrayIndexOutOfBoundsException exception) {
            return 0;
        }
    }

    public float getHeight(float x, float y) {
        try {
            float a1 = x - (int) (x);
            float a2 = y - (int) (y);
            return lerp(lerp(ground[(int)Math.floor(y)][(int)Math.floor(x)],ground[(int)Math.floor(y)][(int)Math.ceil(x)], a1), lerp(ground[(int)Math.ceil(y)][(int)Math.floor(x)], ground[(int)Math.ceil(y)][(int)Math.ceil(x)], a1), a2);
        }
        catch(ArrayIndexOutOfBoundsException exception) {
            return 0;
        }
    }

    private void drawWall(float x, float y, float h, int dir) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshPartBuilder = modelBuilder.part("wall", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(brickTex)));
        if (dir == 0) {
            meshPartBuilder.rect(x+0.5f, h, y, x+0.5f, h, y-0.5f, x+0.5f, h+0.5f, y-0.5f, x+0.5f, h+0.5f, y, 1, 0, 0);
            meshPartBuilder.rect(x+0.5f, h+0.5f, y, x+0.5f, h+0.5f, y-0.5f, x+0.5f, h+1, y-0.5f, x+0.5f, h+1, y, 1, 0, 0);
            meshPartBuilder.rect(x+0.5f, h+0.5f, y+0.5f, x+0.5f, h+0.5f, y, x+0.5f, h+1, y, x+0.5f, h+1, y+0.5f, 1, 0, 0);
            meshPartBuilder.rect(x+0.5f, h, y+0.5f, x+0.5f, h, y, x+0.5f, h+0.5f, y, x+0.5f, h+0.5f, y+0.5f, 1, 0, 0);
        } else if (dir == 1) {
            meshPartBuilder.rect(x-0.5f, h, y, x-0.5f, h, y+0.5f, x-0.5f, h+0.5f, y+0.5f, x-0.5f, h+0.5f, y, -1, 0, 0);
            meshPartBuilder.rect(x-0.5f, h+0.5f, y, x-0.5f, h+0.5f, y+0.5f, x-0.5f, h+1, y+0.5f, x-0.5f, h+1, y, -1, 0, 0);
            meshPartBuilder.rect(x-0.5f, h+0.5f, y-0.5f, x-0.5f, h+0.5f, y, x-0.5f, h+1, y, x-0.5f, h+1, y-0.5f, -1, 0, 0);
            meshPartBuilder.rect(x-0.5f, h, y-0.5f, x-0.5f, h, y, x-0.5f, h+0.5f, y, x-0.5f, h+0.5f, y-0.5f, -1, 0, 0);
        } else if (dir == 2) {
            meshPartBuilder.rect(x, h, y+0.5f, x+0.5f, h, y+0.5f, x+0.5f, h+0.5f, y+0.5f, x, h+0.5f, y+0.5f, 0, 0, 1);
            meshPartBuilder.rect(x, h+0.5f, y+0.5f, x+0.5f, h+0.5f, y+0.5f, x+0.5f, h+1, y+0.5f, x, h+1, y+0.5f, 0, 0, 1);
            meshPartBuilder.rect(x-0.5f, h+0.5f, y+0.5f, x, h+0.5f, y+0.5f, x, h+1, y+0.5f, x-0.5f, h+1, y+0.5f, 0, 0, 1);
            meshPartBuilder.rect(x-0.5f, h, y+0.5f, x, h, y+0.5f, x, h+0.5f, y+0.5f, x-0.5f, h+0.5f, y+0.5f, 0, 0, 1);
        } else if (dir == 3) {
            meshPartBuilder.rect(x, h, y-0.5f, x-0.5f, h, y-0.5f, x-0.5f, h+0.5f, y-0.5f, x, h+0.5f, y-0.5f, 0, 0, -1);
            meshPartBuilder.rect(x, h+0.5f, y-0.5f, x-0.5f, h+0.5f, y-0.5f, x-0.5f, h+1, y-0.5f, x, h+1, y-0.5f, 0, 0, -1);
            meshPartBuilder.rect(x+0.5f, h+0.5f, y-0.5f, x, h+0.5f, y-0.5f, x, h+1, y-0.5f, x+0.5f, h+1, y-0.5f, 0, 0, -1);
            meshPartBuilder.rect(x+0.5f, h, y-0.5f, x, h, y-0.5f, x, h+0.5f, y-0.5f, x+0.5f, h+0.5f, y-0.5f, 0, 0, -1);
        }
        Model wall = modelBuilder.end();
        instances.add(new GameObject(wall));
    }
}
