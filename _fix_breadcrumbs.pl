use strict; use warnings;
local $/ = undef;
my $file = shift @ARGV;
open my $fh, '<', $file or die $!;
my $txt = <$fh>;
close $fh;

# 3-level breadcrumb
$txt =~ s{<nav class="text-sm text-slateish" aria-label="Breadcrumb">\s*<a[^>]*href="([^"]+)"[^>]*>([^<]+)</a>\s*<span class="mx-2(?:\s+text-slate-400)?">/</span>\s*<a[^>]*href="([^"]+)"[^>]*>([^<]+)</a>\s*<span class="mx-2(?:\s+text-slate-400)?">/</span>\s*<span class="text-slate-500">([^<]+)</span>\s*</nav>}{<div class="breadcrumb-pill" aria-label="Breadcrumb"><a href="$1">$2</a><span class="sep">/</span><a href="$3">$4</a><span class="sep">/</span><span class="current">$5</span></div>}gsi;

# 2-level breadcrumb
$txt =~ s{<nav class="text-sm text-slateish" aria-label="Breadcrumb">\s*<a[^>]*href="([^"]+)"[^>]*>([^<]+)</a>\s*<span class="mx-2(?:\s+text-slate-400)?">/</span>\s*<span class="text-slate-500">([^<]+)</span>\s*</nav>}{<div class="breadcrumb-pill" aria-label="Breadcrumb"><a href="$1">$2</a><span class="sep">/</span><span class="current">$3</span></div>}gsi;

open my $out, '>', $file or die $!;
print $out $txt;
close $out;
