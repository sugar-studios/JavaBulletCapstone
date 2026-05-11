package io.github.sugar_studios.javaCapstoneProject;

import com.badlogic.gdx.graphics.Texture;

/** Base class for anything that has health and belongs to a faction. */
public abstract class Entity extends GameObject {

    protected final Faction faction;
    protected final float   maxHealth;
    protected float         currentHealth;

    public Entity(float x, float y, float w, float h, Texture texture,
                  Faction faction, float maxHealth) {
        super(x, y, w, h, texture);
        this.faction       = faction;
        this.maxHealth     = maxHealth;
        this.currentHealth = maxHealth;
    }

    /** Applies damage only if the bullet belongs to the opposing faction. */
    public void onBulletHit(Bullet bullet) {
        if (bullet.getFaction() != this.faction) {
            currentHealth -= bullet.getDamage();
        }
    }

    /** Applies direct damage from a player sword slash. */
    public void onSlashHit(float damage) {
        currentHealth -= damage;
    }

    public void heal(float amount) {
        currentHealth = Math.min(currentHealth + amount, maxHealth);
    }

    public boolean isAlive()          { return currentHealth > 0f; }
    public float   getHealthPercent() { return currentHealth / maxHealth; }
    public Faction getFaction()       { return faction; }
    public float getCurrentHealth() { return currentHealth; }
    public float getMaxHealth()     { return maxHealth; }
}
