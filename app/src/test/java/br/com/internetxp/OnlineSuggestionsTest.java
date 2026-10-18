package br.com.internetxp;

import static org.junit.Assert.*;
import org.junit.Test;

/** Garantias de privacidade: URLs não podem ser usadas como consultas de autocompletar. */
public class OnlineSuggestionsTest {
    @Test public void ordinarySearchTermsAreEligible() {
        assertTrue(OnlineSuggestions.isEligibleQuery("gente não"));
        assertTrue(OnlineSuggestions.isEligibleQuery("youtube música"));
    }
    @Test public void urlsAndLocalAddressesAreNotSent() {
        assertFalse(OnlineSuggestions.isEligibleQuery("https://example.com"));
        assertFalse(OnlineSuggestions.isEligibleQuery("example.com"));
        assertFalse(OnlineSuggestions.isEligibleQuery("example.com/arquivos"));
        assertFalse(OnlineSuggestions.isEligibleQuery("localhost"));
        assertFalse(OnlineSuggestions.isEligibleQuery("192.168.0.1"));
        assertFalse(OnlineSuggestions.isEligibleQuery("router.local"));
    }
    @Test public void unreasonableInputIsBlocked() {
        assertFalse(OnlineSuggestions.isEligibleQuery(""));
        assertFalse(OnlineSuggestions.isEligibleQuery("a"));
        assertFalse(OnlineSuggestions.isEligibleQuery("x".repeat(101)));
        assertFalse(OnlineSuggestions.isEligibleQuery("a\nb"));
    }
}
