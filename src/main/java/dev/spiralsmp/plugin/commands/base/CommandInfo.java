package dev.spiralsmp.plugin.commands.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    String name();
    String description() default "No description provided";
    String permission() default "";
    String[] aliases() default  {};
    boolean requiresCombatCheck() default false;
    boolean requiresCooldownCheck() default false;
}