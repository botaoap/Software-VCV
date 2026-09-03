// SPA deep-link support for the dev server: serve index.html for unknown paths so a fresh load /
// refresh at /catalogo, /produto/{id}, etc. works (matching the RouteCodec). Production hosting needs
// the same rewrite (all paths -> index.html) — see VCV-12.
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
