package helpers;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.mockito.MockitoAnnotations;

public class MockitoTestRunner extends BlockJUnit4ClassRunner {

    public MockitoTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        MockitoAnnotations.initMocks(test);

        return test;
    }
}
