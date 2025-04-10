package com.abwfl.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;

public class Character {
    Decal decal;

    public Character(Texture texture, Vector3 position, float scale) {
        TextureRegion region = new TextureRegion(texture);
        this.decal = Decal.newDecal(region, true);
        this.decal.setPosition(position);
        this.decal.setScale(scale);
        World.decals.add(this.decal);
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
}
