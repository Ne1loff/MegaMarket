package ru.yandex.backendschool.megamarket.validator;

import org.springframework.stereotype.Component;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.exception.badRequest.ValidationError;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Pattern;

@Component(value = "ValidatorImpl")
public class ShopUnitValidatorImpl implements ShopUnitValidator {

    @Override
    public boolean isInvalidUuid(String uuid) {
        if (uuid == null) return true;
        var regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
        return !Pattern.matches(regex, uuid);
    }

    @Override
    public Date validateDateAndGet(String dateString) {
        if (dateString == null) throw new ValidationError();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        try {
            return Date.from(Instant.from(formatter.parse(dateString)));
        } catch (DateTimeParseException exception) {
            throw new ValidationError();
        }

    }


    @Override
    public void validateShopUnitImport(ShopUnitImport unitImport) {

        if (unitImport.getId() == null ||
                unitImport.getName() == null ||
                unitImport.getType() == null
        ) throw new ValidationError();

        if (isInvalidUuid(unitImport.getId())) throw new ValidationError();
        if (unitImport.getParentId() != null && isInvalidUuid(unitImport.getParentId())) throw new ValidationError();

        if (unitImport.getType() == ShopUnitType.CATEGORY && unitImport.getPrice() != null) throw new ValidationError();
        if (unitImport.getType() == ShopUnitType.OFFER && unitImport.getPrice() == null) throw new ValidationError();
    }

}
