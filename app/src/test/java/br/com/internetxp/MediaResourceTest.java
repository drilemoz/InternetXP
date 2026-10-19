package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public class MediaResourceTest {
    @Test public void acceptsOnlyDirectHttpMedia() {
        assertNotNull(MediaResource.from("https://example.org/a.png", "Imagem", "A", false));
        assertNotNull(MediaResource.from("http://localhost:8000/test.jpg", "Imagem", "B", false));
        assertNull(MediaResource.from("blob:https://example.org/abcd", "Vídeo", "", false));
        assertNull(MediaResource.from("data:image/png;base64,AAAA", "Imagem", "", false));
        assertNull(MediaResource.from("javascript:alert(1)", "Imagem", "", false));
        assertNull(MediaResource.from("https://user:password@site.com/a.png", "Imagem", "", false));
        assertNull(MediaResource.from("https://example.org/image", "Arquivo", "", false));
    }

    @Test public void mimeGuessAndStreamingAreConservative() {
        assertEquals("image/png", MediaResource.guessMime("https://example.org/image.png?auth=1", "Imagem"));
        assertEquals("video/mp4", MediaResource.guessMime("https://example.org/video.mp4", "Vídeo"));
        assertEquals("image/*", MediaResource.guessMime("https://example.org/image?token=1", "Imagem"));
        assertTrue(MediaResource.from("https://example.org/a.m3u8", "Vídeo", "", false).isStreamingManifest());
        assertTrue(MediaResource.from("https://example.org/a.mpd", "Vídeo", "", false).isStreamingManifest());
        assertFalse(MediaResource.from("https://example.org/a.mp4", "Vídeo", "", false).isStreamingManifest());
    }

    @Test public void privateAndDescriptionStayScoped() {
        MediaResource media = MediaResource.from("https://example.org/a.png", "Imagem", "A\nB", true);
        assertTrue(media.privateMode);
        assertEquals("A B", media.label);
    }
}
