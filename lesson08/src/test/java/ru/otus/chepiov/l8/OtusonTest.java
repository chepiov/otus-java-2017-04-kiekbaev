package ru.otus.chepiov.l8;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Otuson tests suite.
 * <p>
 * Note that because the JSON format is set of unordered fields and values
 * we can not to check equality of string representations.
 * <p>
 * Instead we will check equality of objects serialized by Otuson and deserialized by Gson.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
public class OtusonTest {

    private Gson gson;
    private Otuson otuson;

    @Before
    public void before() {
        gson = new Gson();
        otuson = new Otuson();
    }

    @Test
    public void testModel() {

        final Model expected = new Model();
        expected.setFirstField("first");
        expected.setSecondField(2);
        expected.setTransientField("transient");

        final Model actual = gson.fromJson(otuson.toJson(expected), Model.class);

        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testArray() {
        final Integer[] expected = new Integer[]{1, 2, 3};
        final Integer[] actual = gson.fromJson(otuson.toJson(expected), Integer[].class);

        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testCollection() {

        final List<Model> actual = new ArrayList<Model>() {{
            final Model firstModel = new Model();
            firstModel.setFirstField("ff");
            firstModel.setSecondField(12);
            firstModel.setTransientField("transient");
            final Model secondModel = new Model();
            secondModel.setFirstField("sf");
            secondModel.setSecondField(22);
            secondModel.setTransientField("transient");
            final Model thirdModel = new Model();
            thirdModel.setFirstField("tf");
            thirdModel.setSecondField(32);
            thirdModel.setTransientField("transient");
            add(firstModel);
            add(secondModel);
            add(thirdModel);
        }};

        TypeToken<List<Model>> token = new TypeToken<List<Model>>() {
        };

        Assert.assertEquals(actual, gson.fromJson(otuson.toJson(actual), token.getType()));

    }
}
