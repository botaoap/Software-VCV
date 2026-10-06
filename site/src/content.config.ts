import { defineCollection, reference } from "astro:content";
import { file } from "astro/loaders";
import { z } from "astro/zod";
import { letterSizes } from "./lib/catalog";

// The catalog mirrors the app's domain model (Product, Collection, FabricSpec, ImageRef) so the two
// stay interchangeable. `reference()` makes a typo'd collection slug fail the build.

/**
 * A photo with required alt text and intrinsic size (so grids never reflow). `url` is a remote URL or
 * a path under src/assets/ ("products/<produto>/<cor>-<vista>.jpg"), self-hosted and optimised.
 */
const photo = z.object({
  url: z.union([z.url(), z.string().regex(/^[a-z0-9/_-]+\.(jpe?g|png|webp|avif)$/, "path under src/assets/")]),
  alt: z.string().min(1),
  width: z.number().int().positive(),
  height: z.number().int().positive(),
});

export const badges = ["BEST_SELLER", "NEW_IN", "LAST_UNITS", "ATELIER_PICK"] as const;

const collectionsCol = defineCollection({
  loader: file("content/collections.json"),
  schema: z.object({
    title: z.string().min(1),
    subtitle: z.string().min(1),
    cover: photo,
    story: z.string().min(1),
  }),
});

/** Garment types shown in the "Produto" menu (Vestidos, Calças, …). Only the types VCV sells. */
const categories = defineCollection({
  loader: file("content/categories.json"),
  schema: z.object({ title: z.string().min(1) }),
});

/** A size label: the letter grade (P, M, G, GG…) or the numeric grade (38–52), always as a string. */
const size = z.union([z.number().int().min(34).max(60).transform(String), z.enum(letterSizes)]);

/** One color of a product (marketplace pattern): its swatch and its own photos, front first. */
const color = z.object({
  id: z.string().regex(/^[a-z0-9-]+$/, "kebab-case id, used in ?cor="),
  name: z.string().min(1),
  /** Swatch only — content data, not a design token. */
  hex: z.string().regex(/^#[0-9A-Fa-f]{6}$/),
  images: z.array(photo).min(1),
});

/** "Medidas da peça" in cm. Columns depend on the garment (bermuda: cintura/quadril; camiseta: busto/barra). */
const sizeChart = z.object({
  columns: z.array(z.string().min(1)).min(1),
  rows: z.array(z.object({ size, values: z.array(z.number().positive()) })).min(1),
  note: z.string().optional(),
});

export const audiences = ["feminino", "masculino", "unissex"] as const;

const products = defineCollection({
  loader: file("content/products.json"),
  schema: z.object({
    name: z.string().min(1),
    collection: reference("collections"),
    category: reference("categories"),
    /** Money is never floating point: integer cents. */
    priceCents: z.number().int().positive(),
    /** Who the garment is for; a catalog filter appears once more than one is on sale. */
    audience: z.enum(audiences).default("feminino"),
    /** Photos shared by every color (all-colors shot, details). Products without colors need ≥ 1. */
    images: z.array(photo).default([]),
    /** Color variants; the PDP gallery and the bag line follow the chosen color. */
    colors: z.array(color).default([]),
    shortDescription: z.string().min(1),
    /** The atelier "ficha técnica" — a brand differentiator. */
    /** Unknown facts are left out (never "a confirmar"): the page shows only what the atelier confirmed. */
    fabric: z
      .object({
        material: z.string().min(1).optional(),
        origin: z.string().min(1).optional(),
        care: z.array(z.string()).default([]),
        notes: z.string().optional(),
      })
      .default({ care: [] }),
    sizes: z.array(size).min(1),
    measurements: sizeChart.optional(),
    badges: z.array(z.enum(badges)),
  }).superRefine((product, ctx) => {
    if (product.images.length === 0 && product.colors.length === 0)
      ctx.addIssue({ code: "custom", path: ["images"], message: "a product needs photos (images or colors)" });
    const ids = product.colors.map((c) => c.id);
    if (new Set(ids).size !== ids.length) ctx.addIssue({ code: "custom", path: ["colors"], message: "duplicate color id" });
    if (new Set(product.sizes).size !== product.sizes.length) ctx.addIssue({ code: "custom", path: ["sizes"], message: "duplicate size" });
    const chart = product.measurements;
    chart?.rows.forEach((row, index) => {
      if (!product.sizes.includes(row.size))
        ctx.addIssue({ code: "custom", path: ["measurements", "rows", index, "size"], message: `size ${row.size} is not sold` });
      if (row.values.length !== chart.columns.length)
        ctx.addIssue({ code: "custom", path: ["measurements", "rows", index, "values"], message: "one value per column" });
    });
  }),
});

const brand = defineCollection({
  loader: file("content/brand.json", { parser: (text) => [{ id: "brand", ...JSON.parse(text) }] }),
  schema: z.object({
    hero: z.array(photo).min(1),
    atelier: z.object({
      headline: z.string().min(1),
      sections: z.array(
        z.object({ title: z.string().min(1), body: z.string().min(1), image: photo.nullable() }),
      ),
    }),
    contact: z.object({
      whatsapp: z.url(),
      instagram: z.url(),
      email: z.email(),
      whereToBuy: z.array(z.string()),
    }),
  }),
});


export const collections = { collections: collectionsCol, categories, products, brand };
