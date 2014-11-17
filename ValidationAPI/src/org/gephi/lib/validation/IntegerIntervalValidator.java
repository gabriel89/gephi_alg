package org.gephi.lib.validation;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexandru Topirceanu
 */
public final class IntegerIntervalValidator implements Validator<String> {

    private int min, max;

    public IntegerIntervalValidator(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean validate(Problems problems, String compName, String model) {
        boolean result = false;
        try {
            Integer i = Integer.parseInt(model);
            result = min <= i && i <= max;
        } catch (Exception e) {
        }
        if (!result) {
            String message = NbBundle.getMessage(IntegerIntervalValidator.class,
                    "IntegerIntervalValidator_OUTSIDE_INTERVAL", model, min, max);
            problems.add(message);
        }
        return result;
    }
}
