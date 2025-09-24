package ewm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageUtils {
    private PageUtils() {}

    public static Pageable offsetBased(int from, int size) {
        int page = from / size;
        return PageRequest.of(page, size);
    }
}
