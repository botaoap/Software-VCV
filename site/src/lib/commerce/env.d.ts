interface ImportMetaEnv {
  /** Commerce adapter: "static" (default) | "nuvemshop". */
  readonly PUBLIC_COMMERCE?: string;
  /** Base URL of VCV's commerce proxy (checkout). Off until it exists. */
  readonly PUBLIC_COMMERCE_PROXY_URL?: string;
  /** Build-time only (never PUBLIC_): the Nuvemshop store and its API token. */
  readonly NUVEMSHOP_STORE_ID?: string;
  readonly NUVEMSHOP_TOKEN?: string;
}
