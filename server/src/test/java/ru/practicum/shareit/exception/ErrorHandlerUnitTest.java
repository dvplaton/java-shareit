package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ErrorHandlerUnitTest {

    private final ErrorHandler handler = new ErrorHandler();

    @Test
    void handleNotFound() {
        ErrorResponse r = handler.handleNotFound(new NotFoundException("нет"));
        assertThat(r.getError(), equalTo("нет"));
    }

    @Test
    void handleConflict() {
        ErrorResponse r = handler.handleConflict(new ConflictException("email"));
        assertThat(r.getError(), equalTo("email"));
    }

    @Test
    void handleForbidden() {
        ErrorResponse r = handler.handleForbidden(new ForbiddenException("forbid"));
        assertThat(r.getError(), equalTo("forbid"));
    }

    @Test
    void handleBadRequest() {
        ErrorResponse r = handler.handleBadRequest(new BadRequestException("bad"));
        assertThat(r.getError(), equalTo("bad"));
    }

    @Test
    void handleUnexpected() {
        ErrorResponse r = handler.handleUnexpected(new RuntimeException("boom"));
        assertThat(r.getError(), equalTo("Внутренняя ошибка сервера"));
    }

    @Test
    void handleUnreadable() {
        ErrorResponse r = handler.handleUnreadable(
                new HttpMessageNotReadableException("x"));
        assertThat(r.getError(), equalTo("Некорректное тело запроса"));
    }

    @Test
    void handleMissingHeader() {
        ErrorResponse r = handler.handleMissingHeader(
                new MissingRequestHeaderException("X-Sharer-User-Id", null));
        assertThat(r.getError(), containsString("X-Sharer-User-Id"));
    }

    @Test
    void handleMissingParam() {
        ErrorResponse r = handler.handleMissingParam(
                new MissingServletRequestParameterException("state", "String"));
        assertThat(r.getError(), containsString("state"));
    }

    @Test
    void handleValidation_withFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "user");
        binding.addError(new FieldError("user", "email", "must not be blank"));

        Method method = Object.class.getDeclaredMethod("toString");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameter, binding);

        ErrorResponse r = handler.handleValidation(ex);
        assertThat(r.getError(), containsString("email"));
    }

    @Test
    void handleValidation_blankMessage() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "user");

        Method method = Object.class.getDeclaredMethod("toString");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(parameter, binding);

        ErrorResponse r = handler.handleValidation(ex);
        assertThat(r.getError(), equalTo("Ошибка валидации"));
    }

    @Test
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex =
                new MethodArgumentTypeMismatchException(
                        "abc", Long.class, "id", null, new IllegalArgumentException("bad"));

        ErrorResponse r = handler.handleTypeMismatch(ex);
        assertThat(r.getError(), containsString("id"));
    }
}