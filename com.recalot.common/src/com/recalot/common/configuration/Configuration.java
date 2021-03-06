// Copyright (C) 2016 Matthäus Schmedding
//
// This file is part of recalot.com.
//
// recalot.com is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// recalot.com is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with recalot.com. If not, see <http://www.gnu.org/licenses/>.
package com.recalot.common.configuration;

import java.lang.annotation.*;

/**
 * @author Matthäus Schmedding (info@recalot.com)
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = Configurations.class)
public @interface Configuration {
    public String key();
    public ConfigurationItem.ConfigurationItemType type() default ConfigurationItem.ConfigurationItemType.String;
    public String value() default "";
    public String description() default "";
    public ConfigurationItem.ConfigurationItemRequirementType requirement() default ConfigurationItem.ConfigurationItemRequirementType.Required;
    public String[] options() default "";
}
