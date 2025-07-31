import org.apache.olingo.commons.api.edm.provider.CsdlAction;

public class temp_test {
    public static void main(String[] args) {
        CsdlAction action = new CsdlAction();
        // Check if CsdlAction has getReturnType method
        System.out.println("CsdlAction methods:");
        for (java.lang.reflect.Method method : CsdlAction.class.getMethods()) {
            if (method.getName().contains("Return") || method.getName().contains("Type")) {
                System.out.println("  " + method.getName() + " : " + method.getReturnType());
            }
        }
    }
}
