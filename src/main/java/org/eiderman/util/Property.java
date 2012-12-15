/**
 * Copyright (C) 2006  Eider Moore
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.eiderman.util;

/**
 * A Property is similiar to an Enum except for a few things
 * <ul>
 * <li>As the name implies, it is dynamic.</li>
 * <li>Every Property has an associated class to use in a property map</li>
 * <li>Propertys can also have a default value for property maps</li>
 * </ul>
 * There is a special note:  This is more of a design pattern than a type, so
 * if you role your own Property, you have to copy and paste the following code:<br>
 * <code>
 *    static Map&lt;String, Property&lt;?&gt;&gt; enums = new LinkedHashMap&lt;String, Property&lt;?&gt;&gt;();<br>
<br>
public static &lt;E&gt; Property&lt;E&gt; create(String name, Class&lt;E&gt; type, E defaultValue)<br>
{<br>
synchronized (enums)<br>
{<br>
Property&lt;E&gt; d = new Property&lt;E&gt;(name, type, defaultValue, enums.size());<br>
enums.put(name, d);<br>
return d;<br>
}<br>
}<br>
<br>
public static Property&lt;?&gt; valueOf(String s)<br>
{<br>
return enums.get(s);<br>
}<br>
<br>
public static Property&lt;?&gt;[] values()<br>
{<br>
synchronized (enums)<br>
{<br>
Property&lt;?&gt;[] result = new Property&lt;?&gt;[enums.size()];<br>
int i = 0;<br>
for (Property&lt;?&gt; d : enums.values())<br>
{<br>
result[i] = d;<br>
}<br>
return result;<br>
}<br>
}<br>
 * </code>
 * @author Eider Moore
 * @version 1
 * @param <T> The type of data held by property.
 */
public class Property<T> {

    protected transient Class<? super T> propertyClass;
    protected transient T defaultValue;
    protected transient int ordinal;
    protected String name;

    public Property(String name, Class<? super T> propertyClass, T defaultValue, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
        this.propertyClass = propertyClass;
        this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public Class<? super T> getPropertyClass() {
        return propertyClass;
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass().equals(obj.getClass())) {
            return ((Property) obj).ordinal() == ordinal();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ordinal;
    }

    @Override
    public String toString() {
        return name;
    }
}
