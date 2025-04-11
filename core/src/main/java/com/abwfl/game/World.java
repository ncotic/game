package com.abwfl.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.math.MathUtils.lerp;

public class World {
    private final int[][] building = {{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 1, 2, 2, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 1, 2, 2, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 0, 2, 2, 2, 0, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    private static float[][] ground;

    public ModelBatch modelBatch;
    public DecalBatch decalBatch;
    public AssetManager assets;
    public final Array<GameObject> instances = new Array<>();
    public static final Array<Character> decals = new Array<>();
    public static final Map<Character, Float> charDistances = new HashMap<>();

    public Model space;

    private Texture brickTex;
    private Texture concreteTex;
    private Texture grassTex;
    private Texture spaceTex;

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

        try {
            BufferedImage noiseBI = ImageIO.read(new File("noise.png"));
            int width = noiseBI.getWidth();
            int height = noiseBI.getHeight();
            ground = new float[height][width];
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    String hexColor = String.format("#%06X", (0xFFFFFF & noiseBI.getRGB(col, row)));
                    ground[row][col] = Color.valueOf(hexColor).r*3-2;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int y = 27; y < 37; y++) {
            for (int x = 27; x < 37; x++) {
                ground[y][x] = 0f;
            }
        }

        new Character(new Texture("suzie.png"), new Vector3(0,.35f,-6), 0.015f, "suzie");
        new Character(new Texture("levan.png"), new Vector3(-2, .35f, -6), 0.003f, "levan");

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
                    GameObject floofobj = new GameObject(floof);
                    floofobj.transform.trn(-8.5f,0f,-5.5f);
                    instances.add(floofobj);
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
            drawWall(x, building.length-1, 0, 2);
            drawWall(x, building.length-1, 1, 2);
            drawWall(x, 0, 1, 3);
        }
        for (int y = 0; y < building.length; y++) {
            drawWall(building[0].length-1, y, 0, 0);
            drawWall(building[0].length-1, y, 1, 0);
            drawWall(0, y, 0, 1);
            drawWall(0, y, 1, 1);
        }
        for (int y = -3; y < 0; y++) {
            for (int x = 0; x < 16; x++) {
                modelBuilder.begin();
                MeshPartBuilder meshPartBuilder = modelBuilder.part("outcon", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(concreteTex)));
                meshPartBuilder.rect(x, 0, y, x, 0, y-0.5f, x-0.5f, 0, y-0.5f, x-0.5f, 0, y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, 0, y, x+0.5f, 0, y-0.5f, x, 0, y-0.5f, x, 0, y, 0, 1, 0);
                meshPartBuilder.rect(x+0.5f, 0, y+0.5f, x+0.5f, 0, y, x, 0, y, x, 0, y+0.5f, 0, 1, 0);
                meshPartBuilder.rect(x, 0, y+0.5f, x, 0, y, x-0.5f, 0, y, x-0.5f, 0, y+0.5f, 0, 1, 0);
                Model outcon = modelBuilder.end();
                GameObject outconobj = new GameObject(outcon);
                outconobj.transform.trn(-8.5f,0f,-5.5f);
                instances.add(outconobj);
            }
        }

        for (int y = 1; y < ground.length-1; y++) {
            for (int x = 1; x < ground[y].length-1; x++) {
                if (!(x >= 28 && x <= 35 && y >= 28 && y <= 35)) {
                    int xLoc = x*2;
                    int yLoc = y*2;
                    modelBuilder.begin();
                    MeshPartBuilder meshPartBuilder = modelBuilder.part("grounds", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(grassTex)));
                    meshPartBuilder.rect(xLoc, ground[y][x], yLoc, xLoc, (ground[y][x]+ground[y-1][x])/2, yLoc-1f, xLoc-1f, (ground[y][x]+ground[y][x-1]+ground[y-1][x-1]+ground[y-1][x])/4, yLoc-1f, xLoc-1f, (ground[y][x]+ground[y][x-1])/2, yLoc, 0, 1, 0);
                    meshPartBuilder.rect(xLoc+1f, (ground[y][x]+ground[y][x+1])/2, yLoc, xLoc+1f, (ground[y][x]+ground[y][x+1]+ground[y-1][x+1]+ground[y-1][x])/4, yLoc-1f, xLoc, (ground[y][x]+ground[y-1][x])/2, yLoc-1f, xLoc, ground[y][x], yLoc, 0, 1, 0);
                    meshPartBuilder.rect(xLoc+1f, (ground[y][x]+ground[y][x+1]+ground[y+1][x+1]+ground[y+1][x])/4, yLoc+1f, xLoc+1f, (ground[y][x]+ground[y][x+1])/2, yLoc, xLoc, ground[y][x], yLoc, xLoc, (ground[y][x]+ground[y+1][x])/2, yLoc+1f, 0, 1, 0);
                    meshPartBuilder.rect(xLoc, (ground[y][x]+ground[y+1][x])/2, yLoc+1f, xLoc, ground[y][x], yLoc, xLoc-1f, (ground[y][x]+ground[y][x-1])/2, yLoc, xLoc-1f, (ground[y][x]+ground[y][x-1]+ground[y+1][x-1]+ground[y+1][x])/4, yLoc+1f, 0, 1, 0);
                    Model grounds = modelBuilder.end();
                    GameObject groundsobj = new GameObject(grounds);
                    groundsobj.transform.trn(-64f,0f,-64f);
                    instances.add(groundsobj);
                }
            }
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
        GameObject wallobj = new GameObject(wall);
        wallobj.transform.trn(-8.5f,0f,-5.5f);
        instances.add(wallobj);
    }

    public void update(Camera camera) {
        instances.get(0).transform.rotate(0.1f, 0.0f, 0.2f,0.1f);
        charDistances.clear();
        for (int i = 0; i < decals.size; i++) {
            double ac = Math.abs(camera.position.z - decals.get(i).getPosition().z);
            double cb = Math.abs(camera.position.x - decals.get(i).getPosition().x);
            float distance = (float) Math.hypot(ac, cb);
            if (distance > 1.9) {
                distance = 1.9f;
            } else if (distance < 1.0) {
                charDistances.put(decals.get(i), distance);
            }
            decals.get(i).decal.setColor(-distance / 2 + 3, -distance / 2 + 3, -distance / 2 + 3, 1);
        }
        charDistances.entrySet()
            .stream()
            .min(Map.Entry.comparingByValue())
            .ifPresentOrElse(closest -> CameraController.closest = closest, () -> CameraController.closest = null);
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
            if (!(x-7.5f < 62 && x-7.5f > -62 && y-4.5f < 62 && y-4.5f > -62)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static float getHeight(float x, float y) {
        try {
            float a1 = x - (int) (x);
            float a2 = y - (int) (y);
            return lerp(lerp(ground[(int)Math.floor(y)][(int)Math.floor(x)],ground[(int)Math.floor(y)][(int)Math.ceil(x)], a1), lerp(ground[(int)Math.ceil(y)][(int)Math.floor(x)], ground[(int)Math.ceil(y)][(int)Math.ceil(x)], a1), a2);
        }
        catch(ArrayIndexOutOfBoundsException exception) {
            return 0;
        }
    }
}
