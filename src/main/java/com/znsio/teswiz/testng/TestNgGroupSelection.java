package com.znsio.teswiz.testng;

import java.util.List;

public record TestNgGroupSelection(List<String> includedGroups, List<String> excludedGroups) {
}
