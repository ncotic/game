package com.abwfl.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class Character {
    Decal decal;
    String name;

    public static boolean speaking = false;

    public Character(Texture texture, Vector3 position, float scale, String name) {
        TextureRegion region = new TextureRegion(texture);
        this.decal = Decal.newDecal(region, true);
        this.decal.setPosition(position);
        this.decal.setScale(scale);
        this.name = name;
        World.decals.add(this);
    }

    public Texture getTexture() {
        return this.decal.getTextureRegion().getTexture();
    }

    public void setTexture(Texture texture) {
        TextureRegion region = new TextureRegion(texture);
        this.decal.setTextureRegion(region);
    }

    public Vector3 getPosition() {
        return this.decal.getPosition();
    }

    public void setPosition(Vector3 position) {
        this.decal.setPosition(position);
    }

    public float[] getScale() {
        return new float[] {this.decal.getScaleX(),this.decal.getScaleY()};
    }

    public void setScale(float scale) {
        this.decal.setScale(scale);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void speak(Character character) {
        try {
            File file = new File("script.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document script = db.parse(file);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Speaking character " + character.getName());
    }
}
