package ru.yandex.backendschool.megamarket.validator;

import org.springframework.stereotype.Component;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.exception.badRequest.ValidationError;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public ZonedDateTime validateDateAndGet(String dateString) {
        if (dateString == null) throw new ValidationError();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        try {
            return ZonedDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException exception) {
            throw new ValidationError();
        }

    }


    @Override
    public void validateShopUnitImport(ShopUnitImport unitImport) {

        if (unitImport.id() == null ||
                unitImport.name() == null ||
                unitImport.type() == null
        ) throw new ValidationError();

        if (isInvalidUuid(unitImport.id())) throw new ValidationError();
        if (unitImport.parentId() != null && isInvalidUuid(unitImport.parentId())) throw new ValidationError();

        if (unitImport.type() == ShopUnitType.CATEGORY && unitImport.price() != null) throw new ValidationError();
        if (unitImport.type() == ShopUnitType.OFFER && unitImport.price() == null) throw new ValidationError();
    }

}
