package ru.yandex.backendschool.megamarket.validator;

import org.junit.jupiter.api.Test;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.exception.badRequest.ValidationError;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShopUnitValidatorImplTest {

    private final ShopUnitValidator validator = new ShopUnitValidatorImpl();

    @Test
    void isValidUuid() {
        String uuid = UUID.randomUUID().toString();
        assertFalse(validator.isInvalidUuid(uuid));
    }

    @Test
    void isInvalidUuid() {
        String uuid = "sdr";
        assertTrue(validator.isInvalidUuid(uuid));
    }

    @Test
    void validateValidDateAndGet() {
        String dateStr = "2022-02-01T12:00:00.000Z";
        ZonedDateTime expectedDate = ZonedDateTime
                .of(2022, 2, 1, 12, 0, 0, 0, ZoneId.of("Z"));
        ZonedDateTime actualDate = validator.validateDateAndGet(dateStr);
        assertEquals(expectedDate, actualDate);
    }

    @Test
    void validateInvalidDateAndThrow() {
        String dateStr = "32sdfrrsa";
        var exception = assertThrows(ValidationError.class, () -> validator.validateDateAndGet(dateStr));
        String expectedMessage = "Validation Failed";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void validShopUnitImport() {
        var uuid = UUID.randomUUID().toString();
        var unitImport = new ShopUnitImport(
                uuid,
                "name",
                null,
                ShopUnitType.OFFER,
                1000L
        );
        validator.validateShopUnitImport(unitImport);
        assertTrue(true);
    }

    @Test
    void invalidShopUnitImport() {
        var unitImport = new ShopUnitImport(
                "uuid",
                "name",
                null,
                ShopUnitType.OFFER,
                1000L
        );
        var exception = assertThrows(ValidationError.class,
                () -> validator.validateShopUnitImport(unitImport));
        String expectedMessage = "Validation Failed";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}