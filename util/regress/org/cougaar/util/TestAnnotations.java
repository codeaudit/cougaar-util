/*
 * <copyright>
 *  
 *  Copyright 1997-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.cougaar.util.annotations.Cougaar;
import org.cougaar.util.annotations.ParameterAnnotations;

/**
 * Test field initialization via annotation metatdata
 * 
 * @author rshapiro
 * 
 */
public class TestAnnotations extends TestCase {
    private static final String GROUP_NAME = "GroupX";
    private static final String PARAMS =
            "SimpleParam=1, SimpleParam=100, ListParam=a, ListParam=b ";

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestArgGroup {
        Cougaar.ParamGroupRole role() default Cougaar.ParamGroupRole.MEMBER;

        Cougaar.ParamGroupIterationPolicy policy() default Cougaar.ParamGroupIterationPolicy.FIRST_UP;
    }

    @Cougaar.ParamGroup(role=Cougaar.ParamGroupRole.OWNER, name=GROUP_NAME)
    public List<Arguments> groupOwner;

    @TestArgGroup(role=Cougaar.ParamGroupRole.OWNER)
    public List<Arguments> testArgGroupOwner;

    @Cougaar.Param(name="SimpleParam")
    @TestArgGroup()
    public int simple;

    @Cougaar.Param(name="SimpleDefaultedParam", defaultValue="10")
    public int simpleDefaulted;

    @Cougaar.Param(name="SimpleDefaultedNullParam", defaultValue=Cougaar.NULL_VALUE)
    public String simpleDefaultedNull;

    @Cougaar.Param(name="ListParam")
    @Cougaar.ParamGroup(name=GROUP_NAME)
    public List<String> list;

    @Cougaar.Param(name="DefaultedListParam", defaultValue="[d,e]")
    public List<String> defaultedList;

    @Cougaar.Param(name="DefaultedListNullParam", defaultValue=Cougaar.NULL_VALUE)
    @Cougaar.ParamGroup(name=GROUP_NAME)
    public List<String> defaultedListNull;

    private void setAll(Arguments args) {
        try {
            new ParameterAnnotations(args).setAllFields(this);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void setGroup(List<Arguments> owner, int index) {
        try {
            new ParameterAnnotations(owner.get(index)).setFields(this);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void setUp() {
        simple = -1;
        simpleDefaulted = -1;
        simpleDefaultedNull = "simpleDefaultedNull";
        list = null;
        defaultedList = null;
        defaultedListNull = new ArrayList<String>();
        testArgGroupOwner = null;
        groupOwner = null;
    }

    public void test_values() {
        assertEquals(simple, -1);
        assertNull(list);
        Arguments arguments = new Arguments(PARAMS);
        setAll(arguments);
        assertEquals(simple, 1);
        assertNotNull(list);
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), "a");
        assertEquals(list.get(1), "b");
    }

    public void test_defaults() {
        assertEquals(simpleDefaulted, -1);
        assertEquals(simpleDefaultedNull, "simpleDefaultedNull");
        assertNull(defaultedList);
        assertNotNull(defaultedListNull);
        Arguments arguments = new Arguments(PARAMS);
        setAll(arguments);
        assertEquals(simpleDefaulted, 10);
        assertNull(simpleDefaultedNull);
        assertNotNull(defaultedList);
        assertEquals(defaultedList.size(), 2);
        assertEquals(defaultedList.get(0), "d");
        assertEquals(defaultedList.get(1), "e");
        assertNull(defaultedListNull);
    }

    public void test_groups() {
        assertNull(testArgGroupOwner);
        assertNull(groupOwner);
        assertNull(list);
        Arguments arguments = new Arguments(PARAMS);
        setAll(arguments);
        assertNotNull(testArgGroupOwner);
        assertEquals(testArgGroupOwner.size(), 2);
        assertNotNull(groupOwner);
        assertEquals(groupOwner.size(), 2);
        setGroup(testArgGroupOwner, 1);
        assertEquals(simple, 100);
        setGroup(testArgGroupOwner, 0);
        assertEquals(simple, 1);

        assertNotNull(list);
        assertEquals(list.size(), 2);
        setGroup(groupOwner, 1);
        assertEquals(list.size(), 1);
        assertEquals(list.get(0), "b");
    }
}