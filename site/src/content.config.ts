import { defineCollection, reference } from "astro:content";
import { file } from "astro/loaders";
import { z } from "astro/zod";

// The catalog mirrors the app's domain model (Product, Collection, FabricSpec, ImageRef) so the two
// stay interchangeable. `reference()` makes a typo'd collection slug fail the build.

/** A photo with required alt text and intrinsic size (so grids never reflow). */
const photo = z.object({
  url: z.url(),
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

const products = defineCollection({
  loader: file("content/products.json"),
  schema: z.object({
    name: z.string().min(1),
    collection: reference("collections"),
    /** Money is never floating point: integer cents. */
    priceCents: z.number().int().positive(),
    images: z.array(photo).min(1),
    shortDescription: z.string().min(1),
    /** The atelier "ficha técnica" — a brand differentiator. */
    fabric: z.object({
      material: z.string().min(1),
      origin: z.string().min(1),
      care: z.array(z.string()),
      notes: z.string().optional(),
    }),
    sizes: z.array(z.number().int().min(34).max(60)).min(1),
    badges: z.array(z.enum(badges)),
    /** External "buy" link (WhatsApp/marketplace); null = only the bag flow is offered. */
    buyUrl: z.url().nullable(),
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


export const collections = { collections: collectionsCol, products, brand };
